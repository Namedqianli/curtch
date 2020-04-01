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
	char points[10][21] = {			//模拟坐标
		"110.274717#22.333378",
		"110.274744#22.333449",
		"110.274789#22.333595",
		"110.274789#22.333595",
		"110.274856#22.333679"
	};
	char sendBuffer[22];			//发送缓存
	float pitch,roll,yaw; 			//欧拉角
	short aacx,aacy,aacz;			//加速度传感器原始数据
	short gyrox,gyroy,gyroz;		//陀螺仪原始数据
	short temp;						//温度	
	u8 i = 0, t = 1;
	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	//设置NVIC中断分组2:2位抢占优先级，2位响应优先级
	delay_init();			//延时初始化
	uart_init(115200);		//串口1初始化115200
	usmart_dev.init(72);	//初始化USMART
	
	while(t){
		t = MPU_Init();
		printf("%d\n",t);
	}
	usart3_init(115200);	//串口3初始化115200
	esp8266_start_trans();	//ESP8266初始化
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
