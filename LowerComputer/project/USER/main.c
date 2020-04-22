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

#define ATGM 1
#define ESP8266 1

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
	char sendBuffer[128];			//���ͻ���
	float pitch,roll,yaw; 			//ŷ����
	short aacx,aacy,aacz;			//���ٶȴ�����ԭʼ����
	short gyrox,gyroy,gyroz;		//������ԭʼ����
	short temp;						//�¶�	
	u8 i = 0, t = 1;
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	//����NVIC�жϷ���2:2λ��ռ���ȼ���2λ��Ӧ���ȼ�
	delay_init();			//��ʱ��ʼ��
	uart_init(9600);		//����1��ʼ��115200
//	printf("A7:00002\r\n");
//	printf("A2\r\n");
#if ATGM
	uart2_init(9600);
	printf("Welcome to use!\r\n");
	printf("ILoveMcu.taobao.com!\r\n");
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
	while(1){
#if ATGM
		parseGpsBuffer();
		//printGpsBuffer();
#endif
#if ESP8266
		memset(sendBuffer, 0, sizeof(sendBuffer));
		sprintf(sendBuffer, "0#%s#%s", Save_Data.latitude, Save_Data.longitude);
		esp8266_send_data_tcp(sendBuffer,20);
#endif
	}
	
	return 0;
}
