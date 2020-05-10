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

#define ATGM 1
#define ESP8266 1
#define MPU 1
#define HCSR04 1
#define LED 1
#define BEEP 1
#define KEY 1

extern _SaveData Save_Data;

int main(void)
{
	char points[10][21] = {			//ģ������
		"110.274717#22.333378",
		"110.274744#22.333449",
		"110.274789#22.333595",
		"110.274789#22.333595",
		"110.274856#22.333679"
	};
	u8 flag = 0 ;
	u8 tooCloseWarning = 0;
	u8 beepDelay = 0;
	float pitch,roll,yaw; 			//ŷ����
	char sendBuffer[128];			//���ͻ���
	short aacx,aacy,aacz;			//���ٶȴ�����ԭʼ����
	short gyrox,gyroy,gyroz;		//������ԭʼ����
	short temp;						//�¶�	
	int distance = 0;
	u8 i = 0, t = 1;
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	//����NVIC�жϷ���2:2λ��ռ���ȼ���2λ��Ӧ���ȼ�
	delay_init();			//��ʱ��ʼ��
	uart_init(9600);		//����1��ʼ��115200
#if ATGM
	uart2_init(9600);
	delay_ms(5);
	clrStruct();
#endif
#if ESP8266
	usart3_init(115200);	//����3��ʼ��115200
	esp8266_start_trans();	//ESP8266��ʼ��
#endif
#if MPU
	usmart_dev.init(72);	//��ʼ��USMART
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
	u2_printf("A7:00003\r\n");
	while(1){
#if ATGM
		parseGpsBuffer();
		//gpsToBaiduMap();
#endif
#if MPU
		MPU_Get_Accelerometer(&aacx,&aacy,&aacz);	//�õ����ٶȴ���������
		MPU_Get_Gyroscope(&gyrox,&gyroy,&gyroz);	//�õ�����������
#endif
#if HCSR04
		distance = HCSR04_Get_Distance();
#endif
		if(KEY_Scan(0) == KEY0_PRES){
			flag = ~flag;
			LED0 = flag;
			LED1 = flag;
		}
#if ESP8266
		//����gps����
		if(Save_Data.isParseData == 1){
			Save_Data.isParseData = 0;
			if(Save_Data.longitude[0] != 0 && Save_Data.latitude[0] != 0){
				memset(sendBuffer, 0, sizeof(sendBuffer));
				sprintf(sendBuffer, "0#%s#%s", Save_Data.latitude, Save_Data.longitude);
				printf("%s", sendBuffer);
				//esp8266_send_data_tcp(sendBuffer,20);
			}
		}
		//�����ϰ������
		if(distance != -1){
			memset(sendBuffer, 0, sizeof(sendBuffer));
			sprintf(sendBuffer, "Distance: %dcm", distance);
			//esp8266_send_data_tcp(sendBuffer, 20);
			if(distance < 10){
				delay_ms(5);
				if(tooCloseWarning == 0){
					tooCloseWarning = 1;
					u2_printf("A7:00001\r\n");	//��������
					LED0 = 1;					//LED0��
					LED1 = 1;					//LED1��
					u2_printf("B4:02\r\n");
				}else{
					beepDelay++;
					if(beepDelay == 10){
						beepDelay = 0;
						BEEP0 = 1;					//��������
					}
				}
			}else{
				if(tooCloseWarning == 1){
					beepDelay = 0;
					tooCloseWarning = 0;
					u2_printf("A4\r\n");
					BEEP0 = 0;
					LED0 = 0;					//LED01��
					LED1 = 0;
					delay_ms(5);
				}
			}
		}
		//����mpu����
		if(mpu_dmp_get_data(&pitch,&roll,&yaw)==0){
			if((roll < -170.0 || pitch > -45.0) || (pitch > 60.0 || pitch < -60.0)){
				LED0 = 1;					//LED0��
				LED1 = 1;					//LED1��
				esp8266_send_data_tcp("1#0#0", 20);
			}else{
				LED0 = 0;					//LED0��
				LED1 = 0;					//LED1��
			}
			memset(sendBuffer, 0, sizeof(sendBuffer));
			delay_ms(5);
			sprintf(sendBuffer, "******MPU6050-Data******\ngyrox:%d\tgyroy:%d\tgyroz:%d", gyrox, gyroy, gyroz);
			esp8266_send_data_tcp(sendBuffer, 20);
			delay_ms(50);
			memset(sendBuffer, 0, sizeof(sendBuffer));
			sprintf(sendBuffer, "******MPU6050-Data******\npitch:%f\troll:%f\tyaw:%f", pitch, roll, yaw);
			esp8266_send_data_tcp(sendBuffer, 20);
		}
#endif
	}
	
	return 0;
}
