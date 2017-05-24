/*******************************************************************************
 * Copyright or © or Copr. 2009 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Jonathan Piat <jpiat@laas.fr> (2009)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2010)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
/* Generated by CIL v. 1.3.6 */
/* print_CIL_Input is false */


void idct1d(int * x , int * Out ) ;

void idct1d(int * x , int * Out) 
{ int _inline_Temp1_1 ;
  int _inline_Temp2_2 ;
  int _inline_Y_3 ;
  int _inline_Z_4 ;
  int _inline_Temp1_5 ;
  int _inline_Temp2_6 ;
  int _inline_Y_7 ;
  int _inline_Z_8 ;
  int _inline_Temp1_9 ;
  int _inline_Y_10 ;
  int _inline_Z_11 ;
  int _inline_Temp1_12 ;
  int _inline_Y_13 ;
  int _inline_Z_14 ;
  int _inline_Temp1_15 ;
  int _inline_Temp2_16 ;
  int _inline_Y_17 ;
  int _inline_Z_18 ;
  int _inline_Temp1_19 ;
  int _inline_Temp2_20 ;
  int _inline_Y_21 ;
  int _inline_Z_22 ;
  int L1[2] ;
  int L2[2] ;
  int xa ;
  int xb ;
  int y[8] ;
  int i ;

  {
  _inline_Temp1_1 = (((x[1] + x[7]) - x[3]) >> 3) - (((x[1] + x[7]) - x[3]) >> 7);
  _inline_Temp2_2 = _inline_Temp1_1 - (((x[1] + x[7]) - x[3]) >> 11);
  _inline_Y_3 = ((x[1] + x[7]) - x[3]) - _inline_Temp1_1;
  _inline_Z_4 = _inline_Temp1_1 + (_inline_Temp2_2 >> 1);
  _inline_Temp1_5 = (((x[1] - x[7]) - x[5]) >> 3) - (((x[1] - x[7]) - x[5]) >> 7);
  _inline_Temp2_6 = _inline_Temp1_5 - (((x[1] - x[7]) - x[5]) >> 11);
  _inline_Y_7 = ((x[1] - x[7]) - x[5]) - _inline_Temp1_5;
  _inline_Z_8 = _inline_Temp1_5 + (_inline_Temp2_6 >> 1);
  _inline_Temp1_9 = (((x[1] + x[7]) + x[3]) >> 9) - ((x[1] + x[7]) + x[3]);
  _inline_Y_10 = (_inline_Temp1_9 >> 2) - _inline_Temp1_9;
  _inline_Z_11 = ((x[1] + x[7]) + x[3]) >> 1;
  _inline_Temp1_12 = (((x[1] - x[7]) + x[5]) >> 9) - ((x[1] - x[7]) + x[5]);
  _inline_Y_13 = (_inline_Temp1_12 >> 2) - _inline_Temp1_12;
  _inline_Z_14 = ((x[1] - x[7]) + x[5]) >> 1;
  _inline_Temp1_15 = x[2] + (x[2] >> 5);
  _inline_Temp2_16 = _inline_Temp1_15 >> 2;
  _inline_Y_17 = _inline_Temp2_16 + (x[2] >> 4);
  _inline_Z_18 = _inline_Temp1_15 - _inline_Temp2_16;
  _inline_Temp1_19 = x[6] + (x[6] >> 5);
  _inline_Temp2_20 = _inline_Temp1_19 >> 2;
  _inline_Y_21 = _inline_Temp2_20 + (x[6] >> 4);
  _inline_Z_22 = _inline_Temp1_19 - _inline_Temp2_20;
  xa = x[1] + x[7];
  xb = x[1] - x[7];
  x[1] = xa + x[3];
  x[3] = xa - x[3];
  x[7] = xb + x[5];
  x[5] = xb - x[5];
  L1[0] = _inline_Y_3;
  L1[1] = _inline_Z_4;
  L2[0] = _inline_Y_7;
  L2[1] = _inline_Z_8;
  x[3] = L1[0] - L2[1];
  x[5] = L2[0] + L1[1];
  L1[0] = _inline_Y_10;
  L1[1] = _inline_Z_11;
  L2[0] = _inline_Y_13;
  L2[1] = _inline_Z_14;
  x[1] = L1[0] + L2[1];
  x[7] = L2[0] - L1[1];
  L1[0] = _inline_Y_17;
  L1[1] = _inline_Z_18;
  L2[0] = _inline_Y_21;
  L2[1] = _inline_Z_22;
  x[2] = L1[0] - L2[1];
  x[6] = L2[0] + L1[1];
  xa = x[0] + x[4];
  xb = x[0] - x[4];
  x[0] = xa + x[6];
  x[6] = xa - x[6];
  x[4] = xb + x[2];
  x[2] = xb - x[2];
  y[0] = x[0] + x[1];
  y[1] = x[4] + x[5];
  y[2] = x[2] + x[3];
  y[3] = x[6] + x[7];
  y[4] = x[6] - x[7];
  y[5] = x[2] - x[3];
  y[6] = x[4] - x[5];
  y[7] = x[0] - x[1];
  i = 0;
    while (i < 8) {
    Out[i] = y[i];
    i ++;
  }
}
}

