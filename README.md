# 20moagm
Ziel des Projekts ist der Entwurf und die Implementierung einer CoronaTrackingApp unter Android. Sie soll die Nachverfolgung von Infektionen erleichtern.

## Ordnerstruktur

- Doku -> Dokumentation
- MyMQTT -> -> TestApp
- QRCodeApp -> TestApp
- NFCDemo -> TestApp
- CreateRoomDemo -> FinaleApp

## Entwurf
Gastgeber können virtuelle Räume erstellen in die sich Nutzer dann beim Betreten physischer Räume per QR-Code oder NFC-Tag eintragen und beim Verlassen wieder Austragen.

Die Ein- und Austrittsdaten werden beim Gastgeber in einer Datenbank hinterlegt.

### Virtuelle Räume

Jeder Raum ist mit einem Timeout ausgestattet. So müssen sich Nutzer nicht Einer nach dem Anderen abmelden wenn Sie den Raum regulär verlassen. Außerdem werden Sie dadurch, falls Sie sich vergessen haben abzumelden, automatisch aus dem Raum entfernt.

Gastgeber können Timeouts auch während einer Sitzung erhöhen oder senken. Zudem können Sie Räume manuell schließen, also alle Nutzer aus dem Raum werfen.

### Kontaktdaten

Nutzer müssen beim ersten Öffnen der App einmalig ihre Kontaktdaten eintragen. Diese bestehen aus:

* Vorname
* Nachname
* Telefonnummer oder Email-Adresse

Die Kontaktdaten werden beim Betreten und Verlassen automatisch beim Gastgeber eingetragen.

### Eingesetzte Technologien

Die Datenbank der aktiven Nutzer pro Raum liegt in der App des Gastgebers. Als Verbindungsprotokoll zwischen Nutzern und Gastgeber wird MQTT oder BLE eingesetzt. Räume werden per NFC pder QR-Code betreten. (Optional) Der Beschleunigungssensor wird für einen Austausch der Benutzerdaten untereinander verwendet.

### Use-Case-Diagramm

Zur besseren Übersicht.

![UseCaseDiagramm](https://drive.google.com/file/d/1Qwc5gvlkLyzoGenEJLtwvRZ0hH-vfiNx/view)



### mqtt password

```
USER    PASSWORD ACCESS
20moagm 1a748f9e ['20moa00/public/#', '20moagm/#', 'public/#']
```
mqtt server funktioniert nicht mehr Quellcode muss entsprechend angepasst werden
