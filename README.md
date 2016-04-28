# mars-rover-photos

mars-rover-photos is a small web application that uses NASA's public API to fetch pictures taken by rovers on Mars (Curiousity, Opportunity and Spirit) on a specific date and creates a gif out of them presenting it in the browser.

## Libs

built with [Compojure](https://github.com/weavejester/compojure), [Ring](https://github.com/ring-clojure/ring) and [Hiccup](https://github.com/weavejester/hiccup)

## Usage

### locally

to run this application locally clone this repository, then run:

```
lein uberjar
```
to create a standalone jar file, then run:
```
java $JVM_OPTS -jar target/mars-rover-photos.jar
```
and open http://localhost:8080/ in you browser.

*PORT*

default port is 8080, but it can be changed by providing an environment variable PORT before starting the application.

### Heroku

there is also an instance running on Heroku https://hidden-chamber-11175.herokuapp.com/

## NASA references

*API*

the API is available for free but a developer key has to be obtained first.

check out the docs here https://api.nasa.gov/api.html#MarsPhotos
