/*Copyright 2016 AETEL
The AETEL Helepolis is released under an Attributtion-ShareAlike 4.0 International (CC BY-SA 4.0) . Therefore, anyone is free to:
•  Share – Copy and redistribute the material in any medium or format.
•  Adapt – Remix, transform, and build upon the material for any purpose, even commercially.
*/
//Code written by Javier Martínez Arrieta to check that all parts of Helepolis work
//Use assembled  Helepolis connected by USB. 
//Check instructions by Serial Monitor at 9600 bauds and if it is correct press main button
//Important: Disconnect bluetooth module or it doesn´t work

#include <Adafruit_NeoPixel.h> 

#define LED_AUTOMATIC 2
#define LED_MANUAL_OFF 0
#define LED_MANUAL_ON 1
#define FORWARD 1
#define BACKWARDS 0
#define STOP 2

#define QRE A7
#define MOT_A_1 7
#define MOT_A_2 3
#define MOT_B_1 4
#define MOT_B_2 5
#define QR_INT 2
#define SHARP_F A6
#define SW 16
#define SW_1 14
#define SW_0 15
#define COD_A 19
#define COD_B 18
#define COD_C 17
#define BUZZ 8
#define LED_FORW 6
#define LED_BACK 9
#define NUMPIXELS 2

//Creates an object to control RGB leds
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, QR_INT, NEO_GRB + NEO_KHZ800);

//Method called when Arduino is switched on
void setup()
{
  //Starts serial 
  Serial.begin(9600); 
  //Starts RGB leds control
  pixels.begin();
}

//Method called after setup is done. This is equivalent to while(true) or while(1)
void loop()
{ 
    Serial.println("AETEL HELEPOLIS TEST\nCheck if the different parts work correctly\nPress the switch to start");
   //Check if switch is pressed. You will not be able to continue if it is not pressed
   while(digitalRead(SW)!=1);

   Serial.println("\nChecking sharp distance sensor. Move your hand or an object to check if value changes. When checked, press the switch");
   //Check that distance is read correctly. Open Serial Monitor and check that values changes when for example you put your hand at different distances
   delay(1000);//Delay for debouncing buttons
   int count=0;
   while(digitalRead(SW)!=1)
   {
      Serial.println(analogRead(SHARP_F));
      delay(100);
   }

   Serial.println("\nCheking microswitch. Put on or off to check that value changes (on->1, off->0). When checked, press the switch");
   //Check microswitch values. Make changes for a few seconds to check that all possible values are correctly read
   delay(1000);//Delay for debouncing buttons
   int sw0,sw1;
   while(digitalRead(SW)!=1)
   {
      sw0=digitalRead(SW_0);
      sw1=digitalRead(SW_1);
      String string="SW0: ";
      string=string+ sw0;
      string=string + "\tSW1: ";
      string=string+ sw1; 
      Serial.println(string);  
      delay(500);
   }

   Serial.println("\nCheck front and back leds. Both should be on.  When checked, press the switch");
   delay(1000);
   while(digitalRead(SW)!=1)
   {
      //Check that front and back leds light correctly
      digitalWrite(LED_FORW,HIGH);
      digitalWrite(LED_BACK,HIGH);
   }
    Serial.println("Switching off leds...");
   //Turn leds off
   digitalWrite(LED_FORW,LOW);
   digitalWrite(LED_BACK,LOW);

   Serial.println("\nBuzzer should be sounding at this moment");
   //Check buzzer. Make it sound for 2 seconds
   analogWrite(BUZZ,255);
   delay(2000);
   //Silence again
   analogWrite(BUZZ,0);

   Serial.println("\nCheking RGB LEDS. Both should light red, green and blue. To start checking, press the button");
   //Check RGB leds, making sure all colours light. The order will be red, green and blue
    while(digitalRead(SW)!=1);
   Serial.println("Lighting red");
    pixels.setPixelColor(0, pixels.Color(255,0,0));
    pixels.setPixelColor(1, pixels.Color(255,0,0));    
    pixels.show();    
    delay(2000);

    Serial.println("Lighting green");
    pixels.setPixelColor(0, pixels.Color(0,255,0));
    pixels.setPixelColor(1, pixels.Color(0,255,0));    
    pixels.show();
    delay(2000);

    Serial.println("Lighting blue");
    pixels.setPixelColor(0, pixels.Color(0,0,255));
    pixels.setPixelColor(1, pixels.Color(0,0,255));    
    pixels.show();   
    delay(2000);
    
    //Switch all colours off
    pixels.setPixelColor(0, pixels.Color(0,0,0));
    pixels.setPixelColor(1, pixels.Color(0,0,0));    
    pixels.show(); 

    Serial.println("\nCheck motors. They should move in both directions, stopping in about 10 seconds. To start checking, press the switch");
    while(digitalRead(SW)!=1);
    //Check motors
    motorControl(FORWARD,255,FORWARD,255);
    delay(5000);
    motorControl(BACKWARDS,255,BACKWARDS,255);
    delay(5000);
    //If properly programmed, PWM value does not matter to stop
    motorControl(STOP,100,STOP,100);
    delay(5000);

    Serial.println("\nChecking white/black sensors. Move the vehicle to check that sensor's value changes when it is above the black line. To start checking, press the switch");
    while(digitalRead(SW)!=1);
    for(count=0;count<50;count++)
    {
        if(count==0)
        {
            Serial.println("Reading QRE0");
        }
        if(count==10)
        {
            Serial.println("Reading QRE1");
        }
        if(count==20)
        {
            Serial.println("Reading QRE2");
        }
        if(count==30)
        {
            Serial.println("Reading QRE3");
        }
        if(count==40)
        {
            Serial.println("Reading QRE4");
        }
        if(count<10)
        {
            read_black_white(0);
        }
        else
        {
            if(count>=10 && count <20)
            {
                read_black_white(1);          
            }
            else
            {
                if(count>=20 && count <30)
                {
                    read_black_white(2);
                }
                else
                {
                    if(count>=30 && count <40)
                    {
                        read_black_white(3);
                    }
                    else
                    {
                        if(count>=40 && count <50)
                        {
                            read_black_white(4);
                        }
                    }
                }
            }  
        }
        delay(1000);
    }
}


//Method to control the speed of both motors and their rotation direction
void motorControl(int motADir, int motASpeed, int motBDir, int motBSpeed){
  if(motADir==FORWARD)
  { 
    digitalWrite(MOT_A_1, LOW);
    analogWrite(MOT_A_2, motASpeed);
  }else{
    if(motADir==BACKWARDS)
    { 
      digitalWrite(MOT_A_1,HIGH);
      analogWrite(MOT_A_2,255-motASpeed);
    }
    else
    {
      digitalWrite(MOT_A_1, LOW);
      analogWrite(MOT_A_2, LOW);
    }
  } 
  if(motBDir==FORWARD)
  { 
    digitalWrite(MOT_B_1, LOW);
    analogWrite(MOT_B_2, motBSpeed);
  }else{
    if(motBDir==BACKWARDS)
    {
      digitalWrite(MOT_B_1, HIGH);
      analogWrite(MOT_B_2, 255-motBSpeed);
    }
    else
    {
      digitalWrite(MOT_B_1, LOW);
      analogWrite(MOT_B_2, LOW);
    }
  }
}

//Method to control both RGB leds. If automatic mode is selected, color level values will be ignored
// pixels.setPixelColor sets RGB the values
// pixels.show sends the updated pixel color to the hardware.
void LedRGBControl(int leftRGBMode,int rightRGBMode,int valLeftR, int valLeftG, int valLeftB,int valRightR, int valRightG, int valRightB,int leftSpeed,int rightSpeed)
{
  if(leftRGBMode==LED_MANUAL_ON)
  {
    pixels.setPixelColor(0, pixels.Color(valLeftR,valLeftG,valLeftB));
    pixels.show(); 
  }
  else
  {
    if(leftRGBMode==LED_AUTOMATIC)
    {
      if(leftSpeed>rightSpeed)
      {
        pixels.setPixelColor(0, pixels.Color(0,255,0)); //Green color
        pixels.show();
      }
      else
      {
        pixels.setPixelColor(0, pixels.Color(0,0,255)); // Blue color
        pixels.show(); 
      }
    }
  }
  if(rightRGBMode==LED_MANUAL_ON)
  {
    pixels.setPixelColor(1, pixels.Color(valRightR,valRightG,valRightB));
    pixels.show();
  }
  else
  {
    if(rightRGBMode==LED_AUTOMATIC)
    {
      if(leftSpeed>rightSpeed)
      {
        pixels.setPixelColor(1, pixels.Color(0,0,255));//Blue color
        pixels.show();
      }
      else
      {
        pixels.setPixelColor(1, pixels.Color(0,255,0)); // Green color.
        pixels.show();
      }
    }    
  }  
}

//Uses multiplexer to read from black/white sensors. COD_C is MSB and COD_A is LSB
void read_black_white(int sensor)
{
  switch (sensor)
  {
    case 0:
    {
      digitalWrite(COD_C,LOW);
      digitalWrite(COD_B,LOW);
      digitalWrite(COD_A,LOW);
      break;
    }
    case 1:
    {
      digitalWrite(COD_C,LOW);
      digitalWrite(COD_B,LOW);
      digitalWrite(COD_A,HIGH);
      break;
    }
    case 2:
    {
      digitalWrite(COD_C,LOW);
      digitalWrite(COD_B,HIGH);
      digitalWrite(COD_A,LOW);
      break;
    }
    case 3:
    {
      digitalWrite(COD_C,LOW);
      digitalWrite(COD_B,HIGH);
      digitalWrite(COD_A,HIGH);
      break;
    }
    case 4:
    {
      digitalWrite(COD_C,HIGH);
      digitalWrite(COD_B,LOW);
      digitalWrite(COD_A,LOW);
      break;
    }
  }
  Serial.println(analogRead(QRE));
}
