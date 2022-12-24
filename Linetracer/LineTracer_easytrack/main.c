#include "msp.h"
#include "Clock.h"
#include <stdio.h>

#define SPEED 3000

void led_init()
{
    // Set P2 as GPIO
    P2->SEL0 &= ~0x07;
    P2->SEL1 &= ~0x07;

    // Input or Output
    // Current type is output
    P2->DIR |= 0x07;

    // Turn off LED
    P2->OUT &= ~0x07;
}

void turn_on_led(int color)
{
    P2->OUT &= ~0x07;
    P2->OUT |= color;
}

void turn_off_led()
{
    P2->OUT &= ~0x07;
}

void systick_init()
{
    SysTick->LOAD = 0x00FFFFFF;
    SysTick->CTRL = 0x00000005;
}

void systick_wait1ms()
{
    SysTick->LOAD = 48000;
    SysTick->VAL = 0;
    while ((SysTick->CTRL & 0x00010000) == 0)
    {
    };
}

void systick_waits()
{
    int i;
    int count = 1000;

    for (i = 0; i < count; i++)
    {
        systick_wait1ms();
    }
}

void pwm_init34(uint16_t period, uint16_t duty3, uint16_t duty4)
{
    TIMER_A0->CCR[0] = period;

    TIMER_A0->EX0 = 0x0000;

    TIMER_A0->CCTL[3] = 0x0040;
    TIMER_A0->CCR[3] = duty3;
    TIMER_A0->CCTL[4] = 0x0040;
    TIMER_A0->CCR[4] = duty4;

    TIMER_A0->CTL = 0x02F0;

    P2->DIR |= 0xC0;
    P2->SEL0 |= 0xC0;
    P2->SEL1 &= ~0xC0;
}

void motor_init(void)
{
    P3->SEL0 &= ~0xC0;
    P3->SEL1 &= ~0xC0;
    P3->DIR |= 0xC0;
    P3->OUT &= ~0xC0;

    P5->SEL0 &= ~0x30;
    P5->SEL1 &= ~0x30;
    P5->DIR |= 0x30;
    P5->OUT &= ~0x30;

    P2->SEL0 &= ~0xC0;
    P2->SEL1 &= ~0xC0;
    P2->DIR |= 0xC0;
    P2->OUT &= ~0xC0;

    pwm_init34(7500, 0, 0);
}

void move(uint16_t leftDuty, uint16_t rightDuty)
{
    P3->OUT |= 0xC0;
    TIMER_A0->CCR[3] = leftDuty;
    TIMER_A0->CCR[4] = rightDuty;
}

void left_forward()
{
    P5->OUT &= ~0x10;
}

void left_backward()
{
    P5->OUT |= 0x10;
}

void right_forward()
{
    P5->OUT &= ~0x20;
}

void right_backward()
{
    P5->OUT |= 0x20;
}

void timer_A3_capture_init()
{
    P10->SEL0 |= 0x30;
    P10->SEL1 &= ~0x30;
    P10->DIR &= ~0x30;

    TIMER_A3->CTL &= ~0x0030;
    TIMER_A3->CTL = 0x0200;

    TIMER_A3->CCTL[0] = 0x4910;
    TIMER_A3->CCTL[1] = 0x4910;
    TIMER_A3->EX0 &= ~0x0007;

    NVIC->IP[3] = (NVIC->IP[3] & 0x0000FFFF) | 0x40400000;
    NVIC->ISER[0] = 0x0000C000;
    TIMER_A3->CTL |= 0x0024;
}

uint16_t first_left;
uint16_t first_right;

uint16_t period_left;
uint16_t period_right;

void TA3_0_IRQHandler(void)
{
    TIMER_A3->CCTL[0] &= ~0x0001;
    period_right = TIMER_A3->CCR[0] - first_right;
    first_right = TIMER_A3->CCR[0];
}

uint32_t left_count;
void TA3_N_IRQHandler(void)
{
    TIMER_A3->CCTL[1] &= ~0x0001;
    left_count++;
}

int sensor1, sensor4, sensor5, sensor8, sensor2, sensor3, sensor6, sensor7;

// ir센서가 main에 있는 while을 한번돌아야만 update되기 때문에 그 사이에 sensor의 변화를 봐야한다면 사용
int ir_update()
{

    // IR on
    P5->OUT |= 0x08;
    P9->OUT |= 0x04;

    P7->DIR = 0xFF;
    P7->OUT = 0xFF;
    Clock_Delay1us(10);

    // read sensor
    P7->DIR = 0x00;
    Clock_Delay1us(1000);
    // Read p7, white = 0

    // IR off
    P5->OUT &= ~0x08;
    P9->OUT &= ~0x04;

    int sensor = P7->IN;
    sensor8 = P7->IN & 0x01;
    sensor1 = P7->IN & 0x80;
    sensor4 = P7->IN & 0x10;
    sensor5 = P7->IN & 0x08;
    sensor2 = P7->IN & 0x40;
    sensor3 = P7->IN & 0x20;
    sensor6 = P7->IN & 0x04;
    sensor7 = P7->IN & 0x02;
    return P7->IN;

}

/**
 * main.c
 */
void main(void)
{
    Clock_Init48MHz();
    led_init();
    systick_init();
    motor_init();
    timer_A3_capture_init();

    int lap, flag;
//    두바퀴확인용
    lap = 0;
//    회전할지 말지 플래그 주기
    flag = 0;

//    ir initialize
    P5->SEL0 &= ~0x08;
    P5->SEL1 &= ~0x08;
    P5->DIR |= 0x08;
    P5->OUT &= ~0x08;

    P9->SEL0 &= ~0x04;
    P9->SEL1 &= ~0x04;
    P9->DIR |= 0x04;
    P9->OUT &= ~0x04;

    P7->SEL0 &= ~0xFF;
    P7->SEL1 &= ~0xFF;
    P7->DIR &= ~0xFF;

    while (1)
    {
        P5->OUT |= 0x08;
        P9->OUT |= 0x04;

        P7->DIR = 0xFF;
        P7->OUT = 0xFF;

        Clock_Delay1us(10);

        P7->DIR = 0x00;

        Clock_Delay1us(1000);

        sensor8 = P7->IN & 0x01;
        sensor1 = P7->IN & 0x80;
        sensor4 = P7->IN & 0x10;
        sensor5 = P7->IN & 0x08;
        sensor2 = P7->IN & 0x40;
        sensor3 = P7->IN & 0x20;
        sensor6 = P7->IN & 0x04;
        sensor7 = P7->IN & 0x02;

//        가운데로 맞추는 용도
        if (sensor4 && sensor5)
        {
            left_forward();
            right_forward();
            move(SPEED, SPEED);
        }
        else if (sensor4 && !sensor5)
        {
            left_backward();
            right_forward();
            move(SPEED, SPEED);
        }
        else if (!sensor4 && sensor5)
        {
            left_forward();
            right_backward();
            move(SPEED, SPEED);
        }

//        몇바퀴 돌았는지 체크
//        왜 맨 위에 놨냐면 출발선에서 좌회전 걸릴 수 있기 때문에
        if ((sensor2 && sensor3 && sensor4 && sensor5 && sensor6 && sensor7
                && !sensor1 && !sensor8)
                || (!sensor2 && sensor3 && sensor4 && sensor5 && sensor6
                        && sensor7 && !sensor1 && sensor8)
                || (sensor2 && sensor3 && sensor4 && sensor5 && sensor6
                        && !sensor7 && sensor1 && !sensor8))
        {
            left_count = 0;
            while (1)
            {
                //                스타트라인이 두꺼우니까
                //                조금앞ㅇ로 가보고 센서 업데이트 후 
                //                똑같이 센서켜져있으면 lap + 1
                if (left_count > 35)
                {
                    ir_update();
                    if ((sensor2 && sensor3 && sensor4 && sensor5 && sensor6 && sensor7
                                    && !sensor1 && !sensor8) || (!sensor2 && sensor3 && sensor4 && sensor5 && sensor6 && sensor7
                                    && !sensor1 && sensor8) || (sensor2 && sensor3 && sensor4 && sensor5 && sensor6 && !sensor7
                                    && sensor1 && !sensor8)
                            {
                                lap += 1;
                                turn_on_led(3 * lap);
                                if (lap >= 1)
                                {
                                    //                            걸리면 무한루프로 멈춤
                                    while (1)
                                    {
                                        move(0, 0);
                                    }
                                }
                            }
                            break;
                        }
                    else
                    {
                        left_forward();
                        right_forward();
                        move(SPEED, SPEED);
                    }
                }
            }

//        좌회전논리와 같음
//        우회전
            if (sensor6 && sensor7 && sensor8)
            {
                left_count = 0;
                while (1)
                {
                    if (left_count > 60)
                    {
                        ir_update();
                        turn_off_led();
                        if (sensor4 || sensor5)
                            break;
                        else
                        {
                            flag = 1;
                            break;
                        }
                    }
                    else
                    {
                        turn_on_led(1);
                        left_forward();
                        right_forward();
                        move(SPEED, SPEED);
                    }
                }
                if (flag == 1)
                {
                    turn_on_led(2);
                    flag = 0;
                    left_count = 0;
                    while (1)
                    {
                        if (left_count > 15)
                            break;
                        else
                        {
                            left_backward();
                            right_backward();
                            move(SPEED / 2, SPEED / 2);
                        }
                    }
                    left_count = 0;
                    while (1)
                    {
                        if (left_count > 180)
                            break;
                        else
                        {
                            left_forward();
                            right_backward();
                            move(SPEED, SPEED);
                        }
                    }
                }
                turn_off_led();
            }

            P5->OUT &= ~0x08;
            P9->OUT &= ~0x04;

            Clock_Delay1ms(10);
        }

    }
