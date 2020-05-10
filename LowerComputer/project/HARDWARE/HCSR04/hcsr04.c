#include "hcsr04.h"
//初始化PA0,PA4 （PA4作为输出口，PA0作为输入口）
//HC_SR04初始化
void HCSR04_Init(u16 arr,u16 psc)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
    TIM_ICInitTypeDef TIM2_ICInitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;


    RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2,ENABLE);//使能TIM2时钟
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA,ENABLE);//使能GPIOA时钟

    GPIO_InitStructure.GPIO_Pin=GPIO_Pin_0;
    GPIO_InitStructure.GPIO_Mode=GPIO_Mode_IPD;//下拉输入
    GPIO_Init(GPIOA,&GPIO_InitStructure);
    GPIO_ResetBits(GPIOA,GPIO_Pin_0);//PA0 下拉

    GPIO_InitStructure.GPIO_Pin=GPIO_Pin_4;
    GPIO_InitStructure.GPIO_Mode=GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed=GPIO_Speed_50MHz;
    GPIO_Init(GPIOA,&GPIO_InitStructure);//PA4 推挽输出

    //初始化定时器 2 TIM2
    TIM_TimeBaseStructure.TIM_Period = arr; //设定计数器自动重装值
    TIM_TimeBaseStructure.TIM_Prescaler =psc; //预分频器
    TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1; //设置时钟分割
    TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //向上计数
    TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure); //初始化 TIM2

    //初始化 TIM2 输入捕获参数
    TIM2_ICInitStructure.TIM_Channel = TIM_Channel_1; //使用CH1
    TIM2_ICInitStructure.TIM_ICPolarity = TIM_ICPolarity_Rising; //上升沿捕获
    TIM2_ICInitStructure.TIM_ICSelection = TIM_ICSelection_DirectTI; //映射到 TI1 上
    TIM2_ICInitStructure.TIM_ICPrescaler = TIM_ICPSC_DIV1; //配置输入分频,不分频
    TIM2_ICInitStructure.TIM_ICFilter = 0x01;//IC1F=0001 配置滤波器 以Fck_int采样，两个事件后有效
    TIM_ICInit(TIM2, &TIM2_ICInitStructure);

    //中断分组初始化
    NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn; //TIM2 中断
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 2; //先占优先级 2 级
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0; //从优先级 0 级
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQ 通道被使能
    NVIC_Init(&NVIC_InitStructure); //初始化外设 NVIC 寄存器
    TIM_ITConfig(TIM2,TIM_IT_Update|TIM_IT_CC1,ENABLE);
    //允许更新中断 CC1IE 捕获中断
    TIM_Cmd(TIM2,ENABLE ); //使能定时器 2
}


//捕获状态
//[7]:0,没有成功的捕获;1,成功捕获到一次.
//[6]:0,还没捕获到高电平;1,已经捕获到高电平了.
//[5:0]:捕获高电平后溢出的次数
u8 TIM2CH1_CAPTURE_STA=0; //输入捕获状态
u16 TIM2CH1_CAPTURE_VAL;  //输入捕获值
//定时器2中断服务程序
void TIM2_IRQHandler(void)
{
    if((TIM2CH1_CAPTURE_STA&0X80)==0)//还未成功捕获
    {
            if (TIM_GetITStatus(TIM2, TIM_IT_Update) != RESET)
        {
            if(TIM2CH1_CAPTURE_STA&0X40)//已经捕获到高电平了
            {
                if((TIM2CH1_CAPTURE_STA&0X3F)==0X3F)//高电平太长了
                {
                    TIM2CH1_CAPTURE_STA|=0X80;//标记成功捕获了一次
                    TIM2CH1_CAPTURE_VAL=0XFFFF;
                }else TIM2CH1_CAPTURE_STA++;
            }
        }
        if (TIM_GetITStatus(TIM2, TIM_IT_CC1) != RESET)//捕获 1 发生捕获事件
        {
            if(TIM2CH1_CAPTURE_STA&0X40) //捕获到一个下降沿
            {
                TIM2CH1_CAPTURE_STA|=0X80; //标记成功捕获到一次上升沿
                TIM2CH1_CAPTURE_VAL=TIM_GetCapture1(TIM2);
                TIM_OC1PolarityConfig(TIM2,TIM_ICPolarity_Rising);
                //CC1P=0 设置为上升沿捕获
            }else //还未开始,第一次捕获上升沿
            {
                TIM2CH1_CAPTURE_STA=0; //清空
                TIM2CH1_CAPTURE_VAL=0;
                TIM_SetCounter(TIM2,0);
                TIM2CH1_CAPTURE_STA|=0X40; //标记捕获到了上升沿
                TIM_OC1PolarityConfig(TIM2,TIM_ICPolarity_Falling);
            //CC1P=1 设置为下降沿捕获
            }
        }
    }
    TIM_ClearITPendingBit(TIM2, TIM_IT_CC1|TIM_IT_Update); //清除中断标志位
}

int HCSR04_Get_Distance()
{
	int i = 0;
	int length = -1;
	int totalLength = 0;
	uint32_t temp = 0;
	
	while(1){
		//发送信号
		Trig_H;
		delay_us(20);
		Trig_L;
		delay_ms(10);
		if(TIM2CH1_CAPTURE_STA&0X80)//成功捕获到了一次高电平
		{
			temp=TIM2CH1_CAPTURE_STA&0X3F;
			temp*=65536; //溢出时间总和
			temp+=TIM2CH1_CAPTURE_VAL; //得到总的高电平时间
			length=temp*0.017; //计算长度
			TIM2CH1_CAPTURE_STA=0; //开启下一次捕获
			totalLength += length;
			i++;
		}
		if(i == 10){
			return totalLength/i;
		}
	}
}