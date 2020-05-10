#include "hcsr04.h"
//��ʼ��PA0,PA4 ��PA4��Ϊ����ڣ�PA0��Ϊ����ڣ�
//HC_SR04��ʼ��
void HCSR04_Init(u16 arr,u16 psc)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
    TIM_ICInitTypeDef TIM2_ICInitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;


    RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2,ENABLE);//ʹ��TIM2ʱ��
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA,ENABLE);//ʹ��GPIOAʱ��

    GPIO_InitStructure.GPIO_Pin=GPIO_Pin_0;
    GPIO_InitStructure.GPIO_Mode=GPIO_Mode_IPD;//��������
    GPIO_Init(GPIOA,&GPIO_InitStructure);
    GPIO_ResetBits(GPIOA,GPIO_Pin_0);//PA0 ����

    GPIO_InitStructure.GPIO_Pin=GPIO_Pin_4;
    GPIO_InitStructure.GPIO_Mode=GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed=GPIO_Speed_50MHz;
    GPIO_Init(GPIOA,&GPIO_InitStructure);//PA4 �������

    //��ʼ����ʱ�� 2 TIM2
    TIM_TimeBaseStructure.TIM_Period = arr; //�趨�������Զ���װֵ
    TIM_TimeBaseStructure.TIM_Prescaler =psc; //Ԥ��Ƶ��
    TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1; //����ʱ�ӷָ�
    TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up; //���ϼ���
    TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure); //��ʼ�� TIM2

    //��ʼ�� TIM2 ���벶�����
    TIM2_ICInitStructure.TIM_Channel = TIM_Channel_1; //ʹ��CH1
    TIM2_ICInitStructure.TIM_ICPolarity = TIM_ICPolarity_Rising; //�����ز���
    TIM2_ICInitStructure.TIM_ICSelection = TIM_ICSelection_DirectTI; //ӳ�䵽 TI1 ��
    TIM2_ICInitStructure.TIM_ICPrescaler = TIM_ICPSC_DIV1; //���������Ƶ,����Ƶ
    TIM2_ICInitStructure.TIM_ICFilter = 0x01;//IC1F=0001 �����˲��� ��Fck_int�����������¼�����Ч
    TIM_ICInit(TIM2, &TIM2_ICInitStructure);

    //�жϷ����ʼ��
    NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn; //TIM2 �ж�
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 2; //��ռ���ȼ� 2 ��
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0; //�����ȼ� 0 ��
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQ ͨ����ʹ��
    NVIC_Init(&NVIC_InitStructure); //��ʼ������ NVIC �Ĵ���
    TIM_ITConfig(TIM2,TIM_IT_Update|TIM_IT_CC1,ENABLE);
    //��������ж� CC1IE �����ж�
    TIM_Cmd(TIM2,ENABLE ); //ʹ�ܶ�ʱ�� 2
}


//����״̬
//[7]:0,û�гɹ��Ĳ���;1,�ɹ�����һ��.
//[6]:0,��û���񵽸ߵ�ƽ;1,�Ѿ����񵽸ߵ�ƽ��.
//[5:0]:����ߵ�ƽ������Ĵ���
u8 TIM2CH1_CAPTURE_STA=0; //���벶��״̬
u16 TIM2CH1_CAPTURE_VAL;  //���벶��ֵ
//��ʱ��2�жϷ������
void TIM2_IRQHandler(void)
{
    if((TIM2CH1_CAPTURE_STA&0X80)==0)//��δ�ɹ�����
    {
            if (TIM_GetITStatus(TIM2, TIM_IT_Update) != RESET)
        {
            if(TIM2CH1_CAPTURE_STA&0X40)//�Ѿ����񵽸ߵ�ƽ��
            {
                if((TIM2CH1_CAPTURE_STA&0X3F)==0X3F)//�ߵ�ƽ̫����
                {
                    TIM2CH1_CAPTURE_STA|=0X80;//��ǳɹ�������һ��
                    TIM2CH1_CAPTURE_VAL=0XFFFF;
                }else TIM2CH1_CAPTURE_STA++;
            }
        }
        if (TIM_GetITStatus(TIM2, TIM_IT_CC1) != RESET)//���� 1 ���������¼�
        {
            if(TIM2CH1_CAPTURE_STA&0X40) //����һ���½���
            {
                TIM2CH1_CAPTURE_STA|=0X80; //��ǳɹ�����һ��������
                TIM2CH1_CAPTURE_VAL=TIM_GetCapture1(TIM2);
                TIM_OC1PolarityConfig(TIM2,TIM_ICPolarity_Rising);
                //CC1P=0 ����Ϊ�����ز���
            }else //��δ��ʼ,��һ�β���������
            {
                TIM2CH1_CAPTURE_STA=0; //���
                TIM2CH1_CAPTURE_VAL=0;
                TIM_SetCounter(TIM2,0);
                TIM2CH1_CAPTURE_STA|=0X40; //��ǲ�����������
                TIM_OC1PolarityConfig(TIM2,TIM_ICPolarity_Falling);
            //CC1P=1 ����Ϊ�½��ز���
            }
        }
    }
    TIM_ClearITPendingBit(TIM2, TIM_IT_CC1|TIM_IT_Update); //����жϱ�־λ
}

int HCSR04_Get_Distance()
{
	int i = 0;
	int length = -1;
	int totalLength = 0;
	uint32_t temp = 0;
	
	while(1){
		//�����ź�
		Trig_H;
		delay_us(20);
		Trig_L;
		delay_ms(10);
		if(TIM2CH1_CAPTURE_STA&0X80)//�ɹ�������һ�θߵ�ƽ
		{
			temp=TIM2CH1_CAPTURE_STA&0X3F;
			temp*=65536; //���ʱ���ܺ�
			temp+=TIM2CH1_CAPTURE_VAL; //�õ��ܵĸߵ�ƽʱ��
			length=temp*0.017; //���㳤��
			TIM2CH1_CAPTURE_STA=0; //������һ�β���
			totalLength += length;
			i++;
		}
		if(i == 10){
			return totalLength/i;
		}
	}
}