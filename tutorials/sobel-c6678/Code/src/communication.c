/*
 * communication.c
 *
 *  Created on: 7 août 2013
 *      Author: Karol
 */
#include <xdc/std.h>
/*  ----------------------------------- IPC module Headers           */
#include <ti/ipc/Ipc.h>
#include <ti/ipc/Notify.h>
#include <ti/sysbios/knl/Semaphore.h>
#include <xdc/runtime/System.h>
#include "communication.h"
#include <ti/sysbios/BIOS.h>
#include <ti/csl/csl_cacheAux.h>
#include <ti/csl/csl_semAux.h>
#include <ti/ipc/MultiProc.h>
#include <ti/sysbios/knl/Task.h>

// 8 local semaphore for each core (1 useless)
Semaphore_Handle interCoreSem[8];

/* Interrupt line used (0 is default) */
#define INTERRUPT_LINE  0

/* Notify event number that the app uses */
#define EVENTID         10

#pragma DATA_SECTION(barrier, ".MSMCSRAM")
Char barrier = 0x00;

Void callbackInterCoreCom(UInt16 procId, UInt16 lineId, UInt32 eventId,
		UArg arg, UInt32 payload) {
	Semaphore_post(interCoreSem[procId]);
}

void sendStart(Uint16 coreID) {
	/* Send an event to the next processor */
	// The last parameter (TRUE) may cause this function to block.
	// In such case, maybe the penultimate parameter could be used to signal
	// that several send are grouped within a single notify event
	Int status = Notify_sendEvent(coreID, INTERRUPT_LINE, EVENTID, 0, TRUE);
	if (status < 0) {
		System_abort("sendEvent failed\n");
	}
}

void receiveEnd(Uint16 coreID){
	Semaphore_pend(interCoreSem[coreID], BIOS_WAIT_FOREVER);
}

void communicationInit() {
	int i;

	/*
	 *  Ipc_start() calls Ipc_attach() to synchronize all remote processors
	 *  because 'Ipc.procSync' is set to 'Ipc.ProcSync_ALL' in *.cfg
	 */
	int status = Ipc_start();
	if (status < 0) {
		System_abort("Ipc_start failed\n");
	}

	// Allocate and initialize a new semaphores and return its handle
	for (i = 0; i < 8; i++) {
		interCoreSem[i] = Semaphore_create(0, NULL, NULL);
	}

	/*
	 *  Register the same callback for all remote processor
	 */
	for (i = 0; i < 8; i++) {
		status = Notify_registerEvent(i, INTERRUPT_LINE, EVENTID,
				(Notify_FnNotifyCbck) callbackInterCoreCom, NULL);
		if (status < 0) {
			System_abort("Notify_registerEvent failed\n");
		}
	}
}

void receiveStart(){}
void sendEnd(){}

void busy_barrier() {
	Uint8 status;
	Char procNumber = MultiProc_self();

	do {
		status = CSL_semAcquireDirect(2);
	} while (status == 0);

	CACHE_invL2(&barrier, 1, CACHE_WAIT);
	barrier |= (1 << procNumber);
	CACHE_wbInvL2(&barrier, 1, CACHE_WAIT);
	CSL_semReleaseSemaphore(2);

	if (procNumber == 0) {
		while (barrier != (Char) 0xFF) {
			Task_sleep(1);
			CACHE_invL2(&barrier, 1, CACHE_WAIT);
		}
		barrier = (Char)0x00;
		sendStart(1);
		receiveEnd(7);
	} else {
		receiveEnd(procNumber-1);
		sendStart((procNumber+1)%8);

	}
}

