#include "stm32f10x.h"
#include "usart.h"
#include "usart3.h"
#include "esp8266.h"
#include "delay.h"
#include "mpu6050.h"
#include "usmart.h"
#include "inv_mpu.h"
#include "inv_mpu_dmp_motion_driver.h"
#include "atgm336h.h"
#include "usart2.h"
#include "hcsr04.h"
#include "led.h"
#include "key.h"
#include "beep.h"

#define DISTANCELIMIT 10		//距离（cm）
#define KEYWARNINGDELAY 20
#define BEEPDELAYKEY 10
#define BEEPTRIPDELAY 20
#define BEEPDISTANCEDELAY 10
#define ATGM 1
#define ESP8266 1
#define MPU 1
#define HCSR04 1
#define LED 1
#define BEEP 1
#define KEY 1

extern _SaveData Save_Data;			//ATGM数据
extern vu16 USART3_RX_STA;			//usart3接收数据标记（ESP8266接收）

int main(void)
{
	char points[10][21] = {			//模拟坐标
		"110.274717#22.333378",
		"110.274744#22.333449",
		"110.274789#22.333595",
		"110.274789#22.333595",
		"110.274856#22.333679"
	};
	u8 tripFlag = 0;				//摔倒标志
	u8 tooCloseFlag = 0;			//接近标志
	u8 keyFlag = 0;					//按键标志
	u8 tooCloseWarning = 0;			//接近延时
	u8 tripDelay = 0;				//摔倒延时
	u8 beepDelay = 0;				//蜂鸣器延时
	u8 beepDelayKey = 0;			//蜂鸣器延时-按键
	u8 keyWarningFlag = 0;			//按键报警
	float pitch,roll,yaw; 			//欧拉角
	char sendBuffer[128];			//发送缓存
	short aacx,aacy,aacz;			//加速度传感器原始数据
	short gyrox,gyroy,gyroz;		//陀螺仪原始数据
	short temp;						//温度	
	int distance = 0;
	u8 i = 0, t = 1;
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	//设置NVIC中断分组2:2位抢占优先级，2位响应优先级
	delay_init();			//延时初始化
	uart_init(115200);		//串口1初始化115200
#if ATGM
	uart2_init(9600);
	delay_ms(5);
	clrStruct();
#endif
	u2_printf("AF:30\r\n");
	delay_ms(100);
	u2_printf("A7:00004\r\n");
#if ESP8266
	usart3_init(115200);	//串口3初始化115200
	esp8266_start_trans();	//ESP8266初始化
#endif
#if MPU
	usmart_dev.init(72);	//初始化USMART
	MPU_Init();
	while(mpu_dmp_init());
#endif
#if HCSR04
	HCSR04_Init(0xffff, 72-1);
#endif
#if LED
	LED_Init();
#endif
#if KEY
	KEY_Init();
#endif
#if BEEP
	BEEP_Init();
#endif
	u2_printf("A7:00005\r\n");
	while(1){
#if ATGM
		parseGpsBuffer();
		//gpsToBaiduMap();
#endif
#if MPU
		MPU_Get_Accelerometer(&aacx,&aacy,&aacz);	//得到加速度传感器数据
		MPU_Get_Gyroscope(&gyrox,&gyroy,&gyroz);	//得到陀螺仪数据
#endif
#if HCSR04
		distance = HCSR04_Get_Distance();			//获取距离
#endif
		if(KEY_Scan(1) == KEY0_PRES){
			keyWarningFlag++;
			if(keyWarningFlag == KEYWARNINGDELAY){
				keyFlag = 1;
				u2_printf("A7:00006\r\n");	//播放语音
				LED0 = 1;					//LED0亮
				LED1 = 1;					//LED1亮
				beepDelayKey++;
				esp8266_send_data_tcp("1#0#0", 50);
				if(beepDelayKey == BEEPDELAYKEY){
					BEEP0 = 1;
				}
			}
		}else{
			beepDelayKey = 0;
			keyWarningFlag = 0;
			keyFlag = 0;
			LED0 = 0;					//LED0灭
			LED1 = 0;					//LED1灭
			BEEP0 = 0;
		}
#if ESP8266
		//发送gps数据
		if(Save_Data.isParseData == 1){
			Save_Data.isParseData = 0;
			if(Save_Data.longitude[0] != 0 && Save_Data.latitude[0] != 0){
				memset(sendBuffer, 0, sizeof(sendBuffer));
				sprintf(sendBuffer, "0#%s#%s", Save_Data.latitude, Save_Data.longitude);
				printf("%s", sendBuffer);
				esp8266_send_data_tcp(sendBuffer,20);
			}
		}
		//发送障碍物情况
		if(distance != -1){
			memset(sendBuffer, 0, sizeof(sendBuffer));
			sprintf(sendBuffer, "Distance: %dcm", distance);
			//esp8266_send_data_tcp(sendBuffer, 20);
			if(distance < DISTANCELIMIT){
				delay_ms(5);
				if(tooCloseWarning == 0){		//延迟一段时间，蜂鸣器再响
					tooCloseWarning = 1;
					u2_printf("A7:00001\r\n");	//播放语音
					LED0 = 1;					//LED0亮
					LED1 = 1;					//LED1亮
					u2_printf("B4:02\r\n");
				}else{
					beepDelay++;
					if(beepDelay == BEEPDISTANCEDELAY){
						beepDelay = 0;
						BEEP0 = 1;					//蜂鸣器响
					}
				}
			}else{
				if(tooCloseWarning == 1){
					beepDelay = 0;
					tooCloseWarning = 0;
					u2_printf("A4\r\n");
					BEEP0 = 0;
					LED0 = 0;					//LED01灭
					LED1 = 0;
					delay_ms(5);
				}
			}
		}
		//发送mpu数据
		if(mpu_dmp_get_data(&pitch,&roll,&yaw)==0){
			if((roll < -170.0 || roll > -45.0) || (pitch > 60.0 || pitch < -60.0)){
				tripDelay++;
				LED0 = 1;
				LED1 = 1;
				if(tripDelay == BEEPTRIPDELAY){
					BEEP0 = 1;
					u2_printf("A7:00002\r\n");
					esp8266_send_data_tcp("1#0#0", 50);
				}
			}else{
				tripDelay = 0;
				BEEP0 = 0;
				LED0 = 0;					//LED0亮
				LED1 = 0;					//LED1亮
			}
			memset(sendBuffer, 0, sizeof(sendBuffer));
			delay_ms(5);
			//sprintf(sendBuffer, "******MPU6050-Data******\ngyrox:%d\tgyroy:%d\tgyroz:%d", gyrox, gyroy, gyroz);
			//esp8266_send_data_tcp(sendBuffer, 20);
			delay_ms(50);
			memset(sendBuffer, 0, sizeof(sendBuffer));
			sprintf(sendBuffer, "******MPU6050-Data******\npitch:%f\troll:%f\tyaw:%f\n", pitch, roll, yaw);
			printf("%s", sendBuffer);
			//esp8266_send_data_tcp(sendBuffer, 20);
		}
		//接收到esp8266数据
		if(USART3_RX_STA & 0x01){
			
		}
#endif
//		if(keyFlag || tripFlag || tooCloseFlag){
//			LED0 = 1;
//			LED1 = 1;
//			if(beepDelayKey == BEEPDELAYKEY || beepDelay == BEEPDISTANCEDELAY || tripDelay == BEEPTRIPDELAY){
//				BEEP0 = 1;
//			}
//		}else{
//			LED0 = 0;
//			LED1 = 0;
//			BEEP0 = 0;
//		}
	}
	
	return 0;
}
