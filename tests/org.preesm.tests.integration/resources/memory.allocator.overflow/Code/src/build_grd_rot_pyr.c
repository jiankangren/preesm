#include <stdlib.h>
#include <float.h>
#include <stdio.h>

#include "ezsift-common.h"


void ITERATOR_build_grd_rot_pyr(int parallelismLevels,
				OUT int * start_octave, OUT int * stop_octave,
				OUT int * start_layer, OUT int * stop_layer,
				OUT int * start_line,  OUT int * stop_line,
				OUT int * start_col, OUT int * stop_col) {

#ifdef SIFT_DEBUG
  fprintf(stderr, "Enter function: %s\n", __FUNCTION__);
#endif
  ITERATOR_generic(parallelismLevels, SIFT_nLayers,
		   start_octave, stop_octave,
		   start_layer, stop_layer,
		   start_line, stop_line,
		   start_col, stop_col);  
}


// Build gradient pyramids.
void build_grd_rot_pyr(int nGpyrLayers, int totSizeWithoutLayers,
		       int parallelismLevels, int nLayers,
		       IN float * gpyr, OUT float * grdPyr,  OUT float * rotPyr, 
		       IN int * start_octave, IN int * stop_octave,
		       IN int * start_layer, IN int * stop_layer,
		       IN int * start_line, IN int * stop_line,
		       IN int * start_col, IN int * stop_col) {
#ifdef SIFT_DEBUG
  fprintf(stderr, "Enter function: %s\n", __FUNCTION__);
#endif
  size_t offset_pyr = 0;
  size_t w = SIFT_IMAGE_W;
  size_t h = SIFT_IMAGE_H;
  if (SIFT_IMG_DBL) {
    w *= 2;
    h *= 2;
  }
  for (int k = 0; k < *start_octave; k++) {
    offset_pyr +=  w*h*nGpyrLayers;
    w /= 2;
    h /= 2;
  }
  
  float * grdData = grdPyr;
  float * rotData = rotPyr;

/* #ifdef SIFT_DEBUG */
/*     char fdog[512]; */
/* #if SIFT_IMG_DBL */
/*     unsigned char buffer[SIFT_IMG_TOT*4]; */
/* #else */
/*     unsigned char buffer[SIFT_IMG_TOT]; */
/* #endif */
/* #endif */

  
  /* fprintf(stderr, "ptr: %lu, %d-%d\t%d-%d\t%d-%d\t%d-%d\n", (unsigned long) grdPyr, *start_octave, *stop_octave, *start_layer, *stop_layer, *start_line, *stop_line, *start_col, *stop_col); */
  for (int i = *start_octave; i < *stop_octave; i++) {
    // We only use gradient information from layers 1~n Layers.
    // Since keypoints only occur at these layers.

    int begin_layer = (i == *start_octave) ? *start_layer+1 : 1;
    int end_layer = (i+1 == *stop_octave) ? *stop_layer+1 : SIFT_nLayers+1;
    size_t index_src = begin_layer*w*h;
    for (int j = begin_layer; j < end_layer; j ++) {
      float * srcData = gpyr + offset_pyr + index_src;
      
      /* fprintf(stderr, "index = %lu\toctave: %d\tlayer: %d\n", (unsigned long) grdData, i, j); */
      
      int begin_line = (i == *start_octave) && (j-1 == *start_layer) ? *start_line : 0;
      int end_line = (i+1 == *stop_octave) && (j == *stop_layer) ? *stop_line : h;
      for (int r = begin_line; r < end_line; r ++) {

	int begin_col = (i == *start_octave) && (j-1 == *start_layer) && (r == *start_line) ? *start_col : 0;
	int end_col = (i+1 == *stop_octave) && (j == *stop_layer) && (r+1 == *stop_line) ? *stop_col : w;
	for (int c = begin_col; c < end_col; c ++) {
	  float dr = get_pixel_f(srcData, w, h, r + 1, c) - get_pixel_f(srcData, w, h, r - 1, c);
	  float dc = get_pixel_f(srcData, w, h, r, c + 1) - get_pixel_f(srcData, w, h, r, c - 1);

#if SIFT_USE_FAST_FUNC
	  *(grdData ++) = fast_sqrt_f(dr * dr + dc * dc);
	  float angle = fast_atan2_f(dr, dc); //atan2f(dr, dc + FLT_MIN);
#else
	  *(grdData ++) = sqrtf(dr * dr + dc * dc);
	  float angle = atan2f(dr, dc + FLT_MIN);
	  angle = angle < 0 ? angle + _2PI : angle;
#endif
	  *(rotData ++) = angle;
	}
/* #ifdef SIFT_DEBUG */
/*       size_t sizeIn = w*h; */
/*       if (begin_line == 0 && r == h - 1 && begin_col == 0 && end_col == w) { */
/* 	sprintf(fdog, "raw_ang-%d-%d.pgm", i, j); */
/* 	write_float_pgm(fdog, rotData-sizeIn, buffer, w, h, 2); */
/*       } */
/* #endif */
      }

      index_src += w * h;
      /* fprintf(stderr, "index_src: %lu\n", index_src); */
    }
    offset_pyr += w*h*nGpyrLayers;
    w /= 2;
    h /= 2;
  }
}
