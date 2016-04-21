/*
AETEL Helepolis
Description: Software for the AETEL Helepolis. It covers 4 modes
  Sumobot WB Mode: The Sumobot Mode programs the AETEL Helepolis to act as a Sumobot. The aim of a Sumobot robot is to Search&Destroy a rival Sumobot.(White Border) 
  Sumobot BB Mode: The Sumobot Mode programs the AETEL Helepolis to act as a Sumobot. The aim of a Sumobot robot is to Search&Destroy a rival Sumobot.(Black Border)
  Line Follower Mode: The Line Follower Mode programs the AETEL Helepolis to follow a Black line.
  Bluetooth Mode: The Bluetooth Mode programs the AETEL Helepolis to act as a Bluetooth remote controlled device.
       
Authors: Pablo de Miguel Morales (Pablodmm.isp@gmail.com)
         Javier Martinez Arrieta (martinezarrietajavier@gmail.com)
         
Release: 18.11.2015
*/

#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL345_U.h>

// Mode definitions
#define LED_MANUAL_OFF 0
#define LED_MANUAL_ON 1
#define LED_AUTOMATIC 2
#define FORWARD 1
#define BACKWARDS 0
#define STOP 2
#define THRESHOLD_DISTANCE 400
#define COWARD_MODE 0
#define SERIAL_KILLER_MODE 1
#define MAX_TIME 3000

// Pin Definitions
#define MUL_A 19
#define MUL_B 18
#define MUL_C 17
#define MOT_A_1 3
#define MOT_A_2 7
#define MOT_B_1 4
#define MOT_B_2 5
#define QRIR 7
#define SHARP 6
#define SW 16
#define SW_1 14
#define SW_0 15
#define BUZZ 8
#define LED_RGB 2
#define LED_FORW 6
#define LED_BACK 9
#define AX_CS 10
#define AX_SDA 11
#define AX_SDO 12
#define AX_SCL 13


const int borderThr = 900;
const int distanceThr = 300;
int inMSS[6];         // incoming serial byte
int speedA = 0;
int speedB = 0;
int ledRv = 0;
int ledGv = 0;
int ledBv = 0;
int ledFORWv = 0;
int ledBACKv = 0;
int dirA = 1;
int dirB = 1;
int QRVal[5] = {0,0,0,0,0};
int ADXVal[3] = {0,0,0};
int CountQR = 0;
boolean borderDetected = 0; // Border Detected Flag
boolean rivalDetected = 0; // Rival Detected Flag
boolean lastDir = 0; // LF Last Direction Flag
int stateSumo=0; // Sumobot Mode State
int stateLineFollower=0; // LineFollower Mode State
int Mode=0; // General Working Mode
int bluetooth_robot_mode;
int dir=STOP;
int leftSpeed=255;
int rightSpeed=255;
int frontLedsMode=0;
int backLedsMode=0;
int leftRGBMode=LED_MANUAL_ON;
int leftRed=255;
int leftGreen=255;
int leftBlue=255;
int rightRGBMode=LED_MANUAL_ON;
int rightRed=255;
int rightGreen=255;
int rightBlue=255;


Adafruit_NeoPixel strip = Adafruit_NeoPixel(2, LED_RGB, NEO_GRB + NEO_KHZ800);


//Adafruit_ADXL345_Unified ADX = Adafruit_ADXL345_Unified(12345);

void setup(){
  Serial.begin(9600);
  pinMode(LED_FORW, OUTPUT);
  pinMode(LED_BACK, OUTPUT);
  pinMode(MUL_A, OUTPUT);
  pinMode(MUL_B, OUTPUT);
  pinMode(MUL_C, OUTPUT);
  pinMode(MOT_A_1, OUTPUT);
  pinMode(MOT_A_2, OUTPUT);
  pinMode(MOT_B_1, OUTPUT);
  pinMode(MOT_B_2, OUTPUT);
  pinMode(SW, INPUT);
  pinMode(SW_0, INPUT);
  pinMode(SW_1, INPUT);
  digitalWrite(LED_FORW,LOW);
  digitalWrite(LED_BACK,LOW);
  strip.begin(); // LED RGB Initialization
  strip.show(); // LED RGB OFF
  //ADX.setRange(ADXL345_RANGE_16_G); // Acc Initialization
  // Mode Selection
  if(digitalRead(SW_0)){
    if (digitalRead(SW_1)){
      Mode = 0; // Sumobot Mode Black Border
    }else{
      Mode = 1; // Sumobot Mode White Border
    }
  }else{
    if (digitalRead(SW_1)){
      Mode = 2; // Line Follower Mode
    }else{
      Mode = 3; // Bluetooth Controlled Mode
      //Starts RGB leds control
      strip.begin();
  
      //Red color for both RGB leds
      LedRGBControl(LED_MANUAL_ON,LED_MANUAL_ON,255,0,0,255,0,0,0,0);
      
      //Wait until it receives which mode is going to be used
      while(Serial.available()==0);
      bluetooth_robot_mode=Serial.read();
      Serial.write(1010);
    }
  }
}

void loop(){
  switch(Mode){
    case 0: // Sumobot Mode Black Border
      SumobotMode();
      ReadIRSensor();
      ReadDistanceSensor();
      break;
    case 1: // Sumobot Mode White Border
      SumobotMode();
      ReadIRSensor();
      ReadDistanceSensor();
      break;
    case 2: // Line Follower Mode
      LineFollowerMode();
      ReadIRSensor();
      ReadDistanceSensor();
      break;
    case 3: // Bluetooth Controlled Mode
      BluetoothMode();
      break;
 }
}

/* Function: motorControl(int motADir, int motASpeed, int motBDir, int motBSpeed)
   This Function controls the motors through 2 directions and 2 speeds [0:255]
*/
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

/* Function: RGBLedControl(int valR_R, int valG_R, int valB_R, int valR_L, int valG_L, int valB_L)
   This Function controls the two RGB WS2812B LED
*/
void RGBLedControl(int valR_R, int valG_R, int valB_R, int valR_L, int valG_L, int valB_L)
{
  strip.setPixelColor(0,valR_R,valG_R,valB_R);
  strip.setPixelColor(1,valR_L,valG_L,valB_L);
  strip.show();
}

/* Function: LedControl(int valFORW, int valBACK)
   This Function controls the four LEDs in pair (one pair FORWARD, on pair BACK)
*/
void LedControl(int valFORW, int valBACK)
{
  analogWrite(LED_FORW, valFORW);
  if(valBACK==0){digitalWrite(LED_BACK,LOW);}else{digitalWrite(LED_BACK,HIGH);}
}

void blueTooth(void)
{
  if(Serial.available()>0)
  {
    int inByte;
    for(int i = 0; i<10; i++)
    {
      inByte=Serial.read();
      inMSS[i]=inByte;
      //if(inByte!='#'){break;}
    }
  }
}

void inMSSread(void)
{
  if(inMSS[2]=='1') // Detects if it is an analog order.
  {
    int speedM;
    if(inMSS[9]=='#'){speedM=(inMSS[7]*10+inMSS[8]);}
    else{speedM=(inMSS[7]*100+inMSS[8]*10+inMSS[9]);}
    if((inMSS[4]=='0')&&(inMSS[5]=='9'))
    {
      speedB=speedM;
    }
    if((inMSS[4]=='1')&&(inMSS[5]=='1'))
    {
      speedA=speedM;
    }
  }
}

/* Function: ReadIRSensor()
   This Function controls the reading of the five IRSensors and activates border detecting flags
*/
void ReadIRSensor()
{
  borderDetected = 0; // Border Detected Flag Reset
  for (int mulCount=0; mulCount<=4; mulCount++) { // QRE1113 Lecture Sensor through 74HC4051 Multiplexer
    // Entry channel selection for 74HC4051
    int cA = mulCount & 0x01;   
    int cB = (mulCount>>1) & 0x01;     
    int cC = (mulCount>>2) & 0x01;    
    digitalWrite(MUL_A, cA);
    digitalWrite(MUL_B, cB);
    digitalWrite(MUL_C, cC);
    // Data analisys and process
    QRVal[mulCount] = analogRead(QRIR);
    switch(Mode){
      case 0: // Sumobot Mode Black Border
        if(QRVal[mulCount]<borderThr){ // Border Detected
        borderDetected = 1;} // Border Detected Flag Set Up
        break;
      case 1: // Sumobot Mode White Border
        if(QRVal[mulCount]>borderThr){ // Border Detected
        borderDetected = 1;} // Border Detected Flag Set Up
        break;
      case 2: // Line Follower Mode
        if(QRVal[mulCount]>borderThr){ // Border Detected
        borderDetected = 1;} // Border Detected Flag Set Up
        break;
    }
  }
}

/* Function:  ReadDistanceSensor()
   This Function controls the reading of SHARP distance sensor
*/
void ReadDistanceSensor()
{
  rivalDetected = 0; // Rival Detected Flag Reset
  if(analogRead(SHARP)>distanceThr){
    rivalDetected = 1;
  }
}


/* Function:  ADXRead(void)
   This Function controls the ADX345 Accelerometer
*/
/*
void ADXRead(void)
{
  sensors_event_t event; 
  ADX.getEvent(&event);
  ADXVal[0] = event.acceleration.x;
  ADXVal[1] = event.acceleration.y;
  ADXVal[2] = event.acceleration.z;
}*/

/* Function:  SumobotMode()
   This Function controls the Sumobot Mode
*/
void SumobotMode(){ // Sumobot Behaviour Mode (Both B/W)
  switch(stateSumo){
    case 0: // Relax
      motorControl(0,0,0,0); // Motors OFF
      LedControl(0,0); // F/B LEDs OFF
      RGBLedControl(0,255,255,0,255,255); // COLOR GB
      if(digitalRead(SW)){ // START SWITCH PRESSED COUNTDOWN 
        stateSumo=1; // go to SB Search State
        for(int i = 0; i<4; i++){
          tone(BUZZ,(160+(i*500)));
          RGBLedControl((50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40))); // Light Blink Progression
          delay(500);
          noTone(BUZZ);
          RGBLedControl(0,0,0,0,0,0);
          delay(500);
         }
       }
       break;
     case 1: // Search
       motorControl(1,200,0,200); // Motors SPIN
       LedControl(255,255); // F/B LEDs ON
       RGBLedControl(255,0,255,255,0,255); // COLOR RB
       // SB State Transition
       if(digitalRead(SW)){stateSumo=0;} // go to SB Relax State
       else if(borderDetected){stateSumo=2;} // go to SB BorderAvoid State
       else if(rivalDetected){stateSumo=3;} // go to SB Attack State
       break;
     case 2: // BorderAvoid
       motorControl(0,200,0,200); // Motors BACK
       LedControl(0,255); // F/B LEDs ON
       RGBLedControl(255,0,0,255,0,0); // COLOR R
       delay(200); // Time
       // SB State Transition
       if(digitalRead(SW)){stateSumo=0;} // go to SB Relax State
       if(!borderDetected){stateSumo=1;} // go to SB BorderAvoid State
       break;
     case 3: // Attack
       motorControl(1,200,1,200); // Motors FORWARD
       LedControl(255,0); // F/B LEDs ON
       RGBLedControl(0,255,0,0,255,0); // COLOR G
       delay(100); // Time
       // SB State Transition
       if(digitalRead(SW)){stateSumo=0;} // go to SB Relax State
       else if(borderDetected){stateSumo=2;} // go to SB BorderAvoid State
       else if(!rivalDetected){stateSumo=1;} // go to SB Search State
       break;
  }
}

/* Function:  LineFollowerMode()
   This Function controls the Line Follower Mode
*/
void LineFollowerMode(){ // Line Follower Behaviour Mode
  
  int speedLeft = 0; // Speed for Left Motor
  int speedRight = 0; // Speed for Right Motor
  switch(stateLineFollower){
  case (0): // Relax
    delay(500);
    motorControl(0,0,0,0); // Motors OFF
    LedControl(0,0); // F/B LEDs OFF
    RGBLedControl(0,255,255,0,255,255); // COLOR GB
    if(digitalRead(SW)){ // START SWITCH PRESSED COUNTDOWN 
      stateLineFollower=1; // go to LN LineSearch State
      for(int i = 0; i<4; i++){
        tone(BUZZ,(160+(i*500)));
        RGBLedControl((50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40)),(50+(i*40))); // Light Blink Progression
        delay(500);
        noTone(BUZZ);
        RGBLedControl(0,0,0,0,0,0);
        delay(500);
       }
     }
     break;
  case(1): // LineSearch
     switch(lastDir){
       case(0):
          motorControl(0,130,1,130); // Motors SPIN
          break;
       case(1):
          motorControl(1,130,0,130); // Motors SPIN
          break;
      }
     RGBLedControl(255,0,0,255,0,0); // COLOR R
     // LN State Transition
     if(digitalRead(SW)){stateLineFollower=0;} // go to LN Relax State
     else if(borderDetected){stateLineFollower=2;} // go to LN InLine State  
     break;
  case (2):
     if(QRVal[2] > borderThr){
       speedLeft = 150; // Base Speed
       speedRight = 150; // Base Speed
       if (QRVal[0] > borderThr){speedRight+=50;}
       if (QRVal[1] > borderThr){speedLeft+=50;}
       motorControl(1,speedLeft,1,speedRight);
       RGBLedControl(0,255,speedLeft,0,255,speedRight); // COLOR G Variable
     }else if(QRVal[3] > borderThr){
       speedLeft = 100; // Base Speed
       speedRight = 150; // Base Speed
       motorControl(1,speedLeft,1,speedRight);
       RGBLedControl(0,0,255,0,0,255); // COLOR B
       lastDir = 0;
     }else if(QRVal[4] > borderThr){
       speedLeft = 150; // Base Speed
       speedRight = 100; // Base Speed
       motorControl(1,speedLeft,1,speedRight);
       RGBLedControl(0,0,0,0,0,255); // COLOR B
       lastDir = 1;
     }
    // LN State Transition
    if(digitalRead(SW)){stateLineFollower=0;} // go to LN Relax State
    else if(!borderDetected){stateLineFollower=1;} // go to LN LineSearch State
    break;
  }  
}

/* Function:  BluetoothMode()()
   This Function controls the Bluetooth controlled device Mode
*/
void BluetoothMode()
{

      //UART sends and receives byte by byte, so when calling Serial.read() 1 byte is read.
      //As UART is asynchronous, timer cannot be used to know when all data is received, but it would be useful to know about connection loss.
      //Wait until all necessary data is available to avoid problems such as reading wrong values (e.g.assining frontLedsMode value to backLedsMode value)     
      while(Serial.available()!=13);
      dir=Serial.read();
      leftSpeed=Serial.read();
      rightSpeed=Serial.read();
      frontLedsMode=Serial.read();
      backLedsMode=Serial.read();
      leftRGBMode=Serial.read();
      leftRed=Serial.read();
      leftGreen=Serial.read();
      leftBlue=Serial.read();
      rightRGBMode=Serial.read();
      rightRed=Serial.read();
      rightGreen=Serial.read();
      rightBlue=Serial.read();
      //If coward mode is selected and an obstacle is detected, forward movements are not allowed, so dir is changed to STOP
      if(bluetooth_robot_mode==COWARD_MODE)
      {
        if(analogRead(SHARP)>=THRESHOLD_DISTANCE&&dir==FORWARD)
        {
          dir=STOP;
        }
      }
      
      //functions
      //Sends direction and speed of each wheel
      motorControl(dir,leftSpeed,dir,rightSpeed);
      
      //Sends direction and front and back leds mode
      front_back_led(dir,frontLedsMode,backLedsMode);
 
      //Sends each RGB mode and values for each color level. If selected mode is automatic, values will not be used.
      LedRGBControl(leftRGBMode,rightRGBMode,leftRed,leftGreen,leftBlue,rightRed,rightGreen,rightBlue,leftSpeed,rightSpeed); 
      Serial.write(1010);//Could be anything, it is to assure that no new data will be sent from Android app
    
    //No delay is required since it is done in the Android application
}

 //Method to control both front and back leds. Dir variable will be used only in case automatic mode is selected
void front_back_led(int dir,int frontLedsMode,int backLedsMode)
{
  switch(frontLedsMode)
  {
    case LED_AUTOMATIC:
    {
      switch(dir)
      {
        case FORWARD:
        {
          digitalWrite(LED_FORW,HIGH);
          break;
        }
        case BACKWARDS:
        {
          digitalWrite(LED_FORW,LOW);
          break;
        }
      };
      break;
    }
    case LED_MANUAL_ON:
    {
      digitalWrite(LED_FORW,HIGH);
      break;
    }
    case LED_MANUAL_OFF:
    {
      digitalWrite(LED_FORW,LOW);
      break;
    }
  };

  switch(backLedsMode)
  {
    case LED_AUTOMATIC:
    {
      switch(dir)
      {
        case FORWARD:
        {
          digitalWrite(LED_BACK,LOW);
          break;
        }
        case BACKWARDS:
        {
          digitalWrite(LED_BACK,HIGH);
          break;
        }
      };
      break;
    }
    case LED_MANUAL_ON:
    {
      digitalWrite(LED_BACK,HIGH);
      break;
    }
    case LED_MANUAL_OFF:
    {
      digitalWrite(LED_BACK,LOW);
      break;
    }
  };
}

//Method to control both RGB leds. If automatic mode is selected, color level values will be ignored
// strip.setPixelColor sets RGB the values
// strip.show sends the updated pixel color to the hardware.
void LedRGBControl(int leftRGBMode,int rightRGBMode,int valLeftR, int valLeftG, int valLeftB,int valRightR, int valRightG, int valRightB,int leftSpeed,int rightSpeed)
{
  if(leftRGBMode==LED_MANUAL_ON)
  {
    strip.setPixelColor(0, strip.Color(valLeftR,valLeftG,valLeftB));
    strip.show(); 
  }
  else
  {
    if(leftRGBMode==LED_AUTOMATIC)
    {
      if(leftSpeed>rightSpeed)
      {
        strip.setPixelColor(0, strip.Color(0,255,0)); //Green color
        strip.show();
      }
      else
      {
        strip.setPixelColor(0, strip.Color(0,0,255)); // Blue color
        strip.show(); 
      }
    }
  }
  if(rightRGBMode==LED_MANUAL_ON)
  {
    strip.setPixelColor(1, strip.Color(valRightR,valRightG,valRightB));
    strip.show();
  }
  else
  {
    if(rightRGBMode==LED_AUTOMATIC)
    {
      if(leftSpeed>rightSpeed)
      {
        strip.setPixelColor(1, strip.Color(0,0,255));//Blue color
        strip.show();
      }
      else
      {
        strip.setPixelColor(1, strip.Color(0,255,0)); // Green color.
        strip.show();
      }
    }    
  }
  
}
