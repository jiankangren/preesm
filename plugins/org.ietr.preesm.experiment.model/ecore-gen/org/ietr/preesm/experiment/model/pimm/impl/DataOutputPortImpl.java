/**
 */
package org.ietr.preesm.experiment.model.pimm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;
import org.ietr.preesm.experiment.model.pimm.PortKind;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Data Output Port</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.ietr.preesm.experiment.model.pimm.impl.DataOutputPortImpl#getOutgoingFifo <em>Outgoing Fifo</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataOutputPortImpl extends DataPortImpl implements DataOutputPort {
  /**
   * The cached value of the '{@link #getOutgoingFifo() <em>Outgoing Fifo</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @see #getOutgoingFifo()
   * @generated
   * @ordered
   */
  protected Fifo outgoingFifo;

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  protected DataOutputPortImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return PiMMPackage.Literals.DATA_OUTPUT_PORT;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public Fifo getOutgoingFifo() {
    if ((this.outgoingFifo != null) && this.outgoingFifo.eIsProxy()) {
      final InternalEObject oldOutgoingFifo = (InternalEObject) this.outgoingFifo;
      this.outgoingFifo = (Fifo) eResolveProxy(oldOutgoingFifo);
      if (this.outgoingFifo != oldOutgoingFifo) {
        if (eNotificationRequired()) {
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO, oldOutgoingFifo, this.outgoingFifo));
        }
      }
    }
    return this.outgoingFifo;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  public Fifo basicGetOutgoingFifo() {
    return this.outgoingFifo;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  public NotificationChain basicSetOutgoingFifo(final Fifo newOutgoingFifo, NotificationChain msgs) {
    final Fifo oldOutgoingFifo = this.outgoingFifo;
    this.outgoingFifo = newOutgoingFifo;
    if (eNotificationRequired()) {
      final ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO, oldOutgoingFifo,
          newOutgoingFifo);
      if (msgs == null) {
        msgs = notification;
      } else {
        msgs.add(notification);
      }
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void setOutgoingFifo(final Fifo newOutgoingFifo) {
    if (newOutgoingFifo != this.outgoingFifo) {
      NotificationChain msgs = null;
      if (this.outgoingFifo != null) {
        msgs = ((InternalEObject) this.outgoingFifo).eInverseRemove(this, PiMMPackage.FIFO__SOURCE_PORT, Fifo.class, msgs);
      }
      if (newOutgoingFifo != null) {
        msgs = ((InternalEObject) newOutgoingFifo).eInverseAdd(this, PiMMPackage.FIFO__SOURCE_PORT, Fifo.class, msgs);
      }
      msgs = basicSetOutgoingFifo(newOutgoingFifo, msgs);
      if (msgs != null) {
        msgs.dispatch();
      }
    } else if (eNotificationRequired()) {
      eNotify(new ENotificationImpl(this, Notification.SET, PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO, newOutgoingFifo, newOutgoingFifo));
    }
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public PortKind getKind() {
    return PortKind.DATA_OUTPUT;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public NotificationChain eInverseAdd(final InternalEObject otherEnd, final int featureID, NotificationChain msgs) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        if (this.outgoingFifo != null) {
          msgs = ((InternalEObject) this.outgoingFifo).eInverseRemove(this, PiMMPackage.FIFO__SOURCE_PORT, Fifo.class, msgs);
        }
        return basicSetOutgoingFifo((Fifo) otherEnd, msgs);
    }
    return super.eInverseAdd(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(final InternalEObject otherEnd, final int featureID, final NotificationChain msgs) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        return basicSetOutgoingFifo(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public Object eGet(final int featureID, final boolean resolve, final boolean coreType) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        if (resolve) {
          return getOutgoingFifo();
        }
        return basicGetOutgoingFifo();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void eSet(final int featureID, final Object newValue) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        setOutgoingFifo((Fifo) newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public void eUnset(final int featureID) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        setOutgoingFifo((Fifo) null);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   *
   * @generated
   */
  @Override
  public boolean eIsSet(final int featureID) {
    switch (featureID) {
      case PiMMPackage.DATA_OUTPUT_PORT__OUTGOING_FIFO:
        return this.outgoingFifo != null;
    }
    return super.eIsSet(featureID);
  }

} // DataOutputPortImpl
