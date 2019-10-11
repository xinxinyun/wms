#include <stdio.h>
#include <stdlib.h>
#include <string.h>


/*====================================================================*/
/*                                                                    */
/* FUNCTION:  str_17to12	                                          */
/*                                                                    */
/*																	   */
/*                                                                     */
/*====================================================================*/
__declspec(dllexport)
int str_17to12(unsigned char* sz12, unsigned char* sz17)
{
	unsigned char	src[18], dst[13];
	int		i, j;
	int		rc = 0;

	if ( NULL == sz12 || NULL == sz17 )
		return -1;

	/* 处理后 4 个字节，存入目标缓冲2个字节
	for ( i=16, j=11; i>=13; i=i-2, j=j-1 ) {
		a = sz17[i] - 0x30;
		a = a & 0x0f;
		b = sz17[i-1] - 0x30;
		b = b & 0x0f;
		b = b<<4;
		sz12[j] = a | b;
	}

	/* 处理12个字节
	for ( ; i>=1; i=i-4, j=j-3 ) {
		a = sz17[i] - 0x30;
		a = a & 0x3f;
		b = sz17[i-1] - 0x30;
		b = b & 0x3f;
		c = sz17[i-2] - 0x30;
		c = c & 0x3f;
		d = sz17[i-3] - 0x30;
		d = d & 0x3f;

		sz12[j]   = (b<<6) | a;
		sz12[j-1] = (c<<4) | (b>>2);
		sz12[j-2] = (c>>4) | (d<<2);
	}

	sz12[0] = sz17[0] - 0x30;
	*/

	if ( NULL == sz12 || NULL == sz17 )
		return -1;

	memset(src, 0, sizeof(src));
	memcpy(src, sz17, 17);
	for ( i=0; i<17; i++ ) {
		src[i] = src[i] - 0x30;
	}

	sz12[11] = (src[16] & 0x0f) | (src[15] & 0x0f) << 4;
	sz12[10] = (src[14] & 0x0f) | (src[13] & 0x0f) << 4;

	sz12[9]  = (src[11] << 6) | (src[12] & 0x3f);
	sz12[8]  = (src[10] << 4) | (src[11] >> 2);
	sz12[7]  = (src[9] << 2)  | (src[10] >> 4);
	sz12[6]  = (src[7] << 6) | (src[8] & 0x3f);
	sz12[5]  = (src[6] << 4) | (src[7] >> 2);
	sz12[4]  = (src[5] << 2)  | (src[6] >> 4);
	sz12[3]  = (src[3] << 6) | (src[4] & 0x3f);
	sz12[2]  = (src[2] << 4) | (src[3] >> 2);
	sz12[1]  = (src[1] << 2)  | (src[2] >> 4);
	sz12[0]  = src[0] & 0x3f;


	return rc;
}

/*====================================================================*/
/*                                                                    */
/* FUNCTION:  str_12to17	                                          */
/*                                                                    */
/*													                  */
/*                                                                    */
/*====================================================================*/
__declspec(dllexport)
int str_12to17(unsigned char* sz17, unsigned char* sz12)
{
	int		i, j;
	int		rc = 0;
	unsigned char a, b, c;

	sz17[16] = sz12[11] & 0x0f;
	sz17[15] = (sz12[11] & 0xf0) >> 4;
	sz17[14] = sz12[10] & 0x0f;
	sz17[13] = (sz12[10] & 0xf0) >> 4;

	sz17[12] = sz12[9] & 0x3f;
	sz17[11] = ((sz12[9] & 0xc0)>>6) | ((sz12[8] & 0x0f)<<2);
	sz17[10] = ((sz12[8] & 0xf0)>>4) | ((sz12[7] & 0x03)<<4);
	sz17[9]  = (sz12[7] & 0xfc)>>2;
	sz17[8]  = sz12[6] & 0x3f;
	sz17[7]  = ((sz12[6] & 0xc0)>>6) | ((sz12[5] & 0x0f)<<2);
	sz17[6]  = ((sz12[5] & 0xf0)>>4) | ((sz12[4] & 0x03)<<4);
	sz17[5]  = (sz12[4] & 0xfc)>>2;
	sz17[4]  = sz12[3] & 0x3f;
	sz17[3]  = ((sz12[3] & 0xc0)>>6) | ((sz12[2] & 0x0f)<<2);
	sz17[2]  = ((sz12[2] & 0xf0)>>4) | ((sz12[1] & 0x03)<<4);
	sz17[1]  = (sz12[1] & 0xfc)>>2;
	sz17[0]  = sz12[0] & 0x3f;

	for ( i=0; i<17; i++ ) {
		sz17[i] = sz17[i] + 0x30;
	}

	return rc;
}

int main()
{


}