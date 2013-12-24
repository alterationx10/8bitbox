/**

8BitBox: An open hardware and software project
 
http://markslaboratory.com/8bitbox

**/

// Define our pins
int buzzerPin = 3;  // Piezo buzzer is hooked to pwm 3
int redPin = 9;     // Red LED hooked to pwm 9
int greenPin = 10;  // Green LED hooked to pwm 10
int bluePin = 11;   // Blue LED hooked to pwm 11

int commandByte;   // A variable for command parsing


void setup(void) 
{
  // Set up our pins
  pinMode(buzzerPin, OUTPUT);//buzzer
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);
  
  // Start Serial
  Serial.begin(9600);
  
  // Run our start-up sequence
  bootSequence();

}

void loop() {
  
   
  // All our loop currently does is check for serial data
  // and parse serial commands
  if (Serial.available() > 0) {
    
    // Read our command byte
    commandByte = Serial.read();
    // See if it maches any of our commands
   
    // Set the Red LED
    if (commandByte == 'R') {
      int redVal =  getNextByte();
      setRed(redVal);
    }

    // Set the Green LED
    else if (commandByte == 'G') {
      int greenVal =  getNextByte();
      setGreen(greenVal);
    }
   
    // Set the Blue LED
    else if (commandByte == 'B') {
      int blueVal =  getNextByte();
      setBlue(blueVal);
    }
   
   // Play a note
   // Playing lots of notes makes a song!; See the line 'Serial.print("z")' about the relevance to this
   else if (commandByte == 'Z') {
     
     // Get the note.
     // We are sending byte[] over SPP from Android, and the values
     // of the notes go higher than 255, so we need to peice back together
     // our (16 bit) int value. Order is MSB then LSB!
     int buzzNoteMSB = getNextByte();
     int buzzNoteLSB = getNextByte();
     int buzzNote = (buzzNoteMSB << 8) + buzzNoteLSB; 
     
     // Get the tempo, and calculate the beats per second this note is to play for.
     // For example, if buzzTempo is 4, then buzzDuration will be 1000/4; 4 beats per second.
     int buzzTempo = getNextByte();
     int buzzDuration = 1000/buzzTempo;
     
     // Play the note
     buzz(buzzerPin, buzzNote, buzzDuration);
     
     // Signal we're done
     // Note:
     // We have offloaded our songs and melodies from an Arduino library file, to a Java file in Android.
     // This way, adding more songs/melodies can be done in the App and not here in the firmware.
     // Because we are sending a lot of notes very quickly in the case of a song, we will likely throw off the tempo (timinig)
     // or overflow the serial buffer, of just generally bork it up.
     // To counter this, we block the ndorid app until we send back this next character. Since there is a delay here, after this method,
     // the next buzz command is ready to be played and we don't mess up our timing.
     // Haven't had any troubles with this... YET. :-)
     Serial.print("z");
      
      
     // Add a small delay to distinguish between multiple notes
     int pauseBetweenNotes = buzzDuration * 1.30;
     delay(pauseBetweenNotes);
     
     // stop the tone playing:
      buzz(buzzerPin, 0, buzzDuration);
   }
   
  }
   
}


// Buzz a tone on the piezo buzzer
void buzz(int targetPin, long frequency, long length) {
  long delayValue = 1000000/frequency/2; // calculate the delay value between transitions
  //// 1 second's worth of microseconds, divided by the frequency, then split in half since
  //// there are two phases to each cycle
  long numCycles = frequency * length/ 1000; // calculate the number of cycles for proper timing
  //// multiply frequency, which is really cycles per second, by the number of seconds to 
  //// get the total number of cycles to produce
  for (long i=0; i < numCycles; i++){ // for the calculated length of time...
    digitalWrite(targetPin,HIGH); // write the buzzer pin high to push out the diaphram
    delayMicroseconds(delayValue); // wait for the calculated delay value
    digitalWrite(targetPin,LOW); // write the buzzer pin low to pull back the diaphram
    delayMicroseconds(delayValue); // wait again or the calculated delay value
  }
}

// Serial.read() appears to go much faster than we can send text.
// Therefore, we add a method to do a blocking read.
int getNextByte() {
  while (Serial.available() == 0) {
    // BLOCK
  }
  return Serial.read();
}

// Something to do when the 8BitBox is first powered on
void bootSequence() {
    setColor(255, 0, 0);  // red
  delay(500);
  setColor(0, 255, 0);  // green
  delay(500);
  setColor(0, 0, 255);  // blue
  delay(500);
  setColor(255, 255, 0);  // yellow
  delay(500);  
  setColor(80, 0, 80);  // purple
  delay(500);
  setColor(0, 255, 255);  // aqua
  delay(500);
  setColor(0,0,0);
}

// Set the PWM of the Red LED
void setRed(int val) {
  // Sanitize the value
  if (val > 255) {
    val = 255;
  }
  if (val < 0) {
    val = 0;
  }  
  analogWrite(redPin, 255 - val);
}

// Set the PWM of the Green LED
void setGreen(int val) {
  // Sanitize the value
  if (val > 255) {
    val = 255;
  }
  if (val < 0) {
    val = 0;
  }  
  analogWrite(greenPin, 255 - val);
}

// Set the PWM of the Blue LED
void setBlue(int val) {
  // Sanitize the value
  if (val > 255) {
    val = 255;
  }
  if (val < 0) {
    val = 0;
  }  
  analogWrite(bluePin, 255 - val);
}
  
  
// Set all the colors at once  
void setColor(int red, int green, int blue) {
  setRed(red);
  setGreen(green);
  setBlue(blue); 
}
