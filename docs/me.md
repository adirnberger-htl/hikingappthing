# Hiking App thing

So I want to create a desktop app that pull routes from komoot and can print a fun map viso of the last hikes / runs / bikes (it should be configurable via a config file) into the terminal. It should also print certain stats depending ontype of sport.

In the end I will intigrate it in a fish (or bash) command.

## Spec

### Visu

Yh so ahm I want it to be like an Ascii art thing with like an orange line that looks cool and stuff and it needs to work with transparent terminals cause mine is and then it has to look cool with my background which is purple at the moment and dark. yh....

### Interaction

I'll just say like **peaks** and then it shows me my last bagged peaks and then it'll be very cool to show off.

## Impl

### tech

Do it on Java and avoid any unnecisarry libraries and stuff.

### komoot 

gemini said that: 

```
2. Standard Web Embedding

If you just want to showcase specific Komoot routes, trails, or collections within your app or website, Komoot provides an official embedding feature.

    How it works: You can generate an HTML <iframe> code snippet directly from any public tour or smart tour on the Komoot website.

    What you get: An interactive map with an elevation profile that users can see directly in your app's web-view interface, though you won't get access to the raw data points (like GPX coordinates).

```