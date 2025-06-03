// Pines en el ESP32
const int TRIG_PIN       = 5;   // D5
const int ECHO_PIN       = 18;  // D18
const int LED_RED_PIN    = 22;  // D22
const int LED_GREEN_PIN  = 21;  // D21

void setup() {
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(LED_RED_PIN, OUTPUT);
  pinMode(LED_GREEN_PIN, OUTPUT);

  digitalWrite(LED_RED_PIN, LOW);
  digitalWrite(LED_GREEN_PIN, LOW);

  Serial.begin(9600);
  Serial.println("HC-SR04 + LEDs inicializado");
}

void loop() {
  // Generar pulso de trigger
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  // Leer eco
  long duration = pulseIn(ECHO_PIN, HIGH);

  // Calcular distancia
  float distance = duration * 0.034f / 2.0f;

  Serial.printf("Distancia: %.2f cm\n", distance);

  // Lógica de LEDs
  if (distance > 0 && distance <= 10.0f) {
    digitalWrite(LED_RED_PIN, HIGH);
    digitalWrite(LED_GREEN_PIN, LOW);
  } else {
    digitalWrite(LED_RED_PIN, LOW);
    digitalWrite(LED_GREEN_PIN, HIGH);
  }

  delay(1000);
}
