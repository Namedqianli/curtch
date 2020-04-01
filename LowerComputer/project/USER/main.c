#include "stm32f10x.h"
#include "usart.h"
#include "usart3.h"
#include "esp8266.h"
#include "delay.h"
#include "mpu6050.h"
#include "usmart.h"   
#include "inv_mpu.h"
#include "inv_mpu_dmp_motion_driver.h" 

int main(void)
{
	char points[10][21] = {			//ģ������
		"110.274717#22.333378",
		"110.274744#22.333449",
		"110.274789#22.333595",
		"110.274789#22.333595",
		"110.274856#22.333679"
	};
	char sendBuffer[22];			//���ͻ���
	float pitch,roll,yaw; 			//ŷ����
	short aacx,aacy,aacz;			//���ٶȴ�����ԭʼ����
	short gyrox,gyroy,gyroz;		//������ԭʼ����
	short temp;						//�¶�	
	u8 i = 0, t = 1;
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	//����NVIC�жϷ���2:2λ��ռ���ȼ���2λ��Ӧ���ȼ�
	delay_init();			//��ʱ��ʼ��
	uart_init(115200);		//����1��ʼ��115200
	usmart_dev.init(72);	//��ʼ��USMART
	
	while(t){
		t = MPU_Init();
		printf("%d\n",t);
	}
	usart3_init(115200);	//����3��ʼ��115200
	esp8266_start_trans();	//ESP8266��ʼ��
	while(1){
		memset(sendBuffer, 0, sizeof(sendBuffer));
		sprintf(sendBuffer, "0#%s", points[i]);
		esp8266_send_data_tcp(sendBuffer,20);
		delay_ms(1000);
		delay_ms(1000);
		delay_ms(1000);
		delay_ms(1000);
		delay_ms(1000);
		delay_ms(1000);
		i++;
		if(i >= 5) i = 0;
		printf("-----------------------\n");
	}
	
	return 0;
}
