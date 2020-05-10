#ifndef _HCSR04_H
#define _HCSR04_H

#include "sys.h"
#include "delay.h"

#define Trig_H  GPIO_SetBits(GPIOA, GPIO_Pin_4)
#define Trig_L  GPIO_ResetBits(GPIOA, GPIO_Pin_4)

void HCSR04_Init(u16 arr,u16 psc);
int HCSR04_Get_Distance();

#endif