![Alt text](images/RavenVBanner.png)
# Raven V

### Raven V isn't just another Raven build with 2 new modules... 
V fully re-writes many of Raven's core architectures and adds a translation layer which allows for kotlin based LiquidBounce modules to be run as if they were Raven modules.

### Full UI Overhaul.
All around improvements have been made to the UI make it more modern and easier on the eyes. Before it looked like outdated and low effort.

## The translation of Liquid Bounce's Kotlin Modules is not a straightforward one.
Liquid Bounce is built on Minecraft 1.21+ and so many if not all of their event classes use Minecraft functions that don't exist in 1.8.9. Along with the fact that we aren't using the UI and Rendering systems that LiquidBounce does so adding conversions for all of that was also considered. There are other hurdles that needed to be figured out but that will be added here at a later date.


## Building

```bash
./gradlew build
```



### Disclaimer: Large amounts of code is taken directly from Liquid Bounce, this client is open source and not being created for profit. If this client ever goes closed source it will not be sold or distributed and vum is not responsible for the actions of anyone that does so.
