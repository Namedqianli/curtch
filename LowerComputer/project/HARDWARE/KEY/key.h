#ifndef _KEY_H
#define _KEY_H

#include "sys.h"
#include "delay.h"

#define KEY0  GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_1)		//读取按键0
#define KEY1  GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_0)		//读取按键1
 
#define KEY0_PRES 	1	//KEY0按下
#define KEY1_PRES	2	//KEY1按下
#define WKUP_PRES   3	//KEY_UP按下(即WK_UP/KEY_UP)


void KEY_Init(void);//IO初始化
u8 KEY_Scan(u8);  	//按键扫描函数	

#endif