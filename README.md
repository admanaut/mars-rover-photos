# mars-rover-photos

Web app that allows users to track the progress of NASA's rovers on Mars (Curiosity, Opportunity and Spirit). The app uses NASA's public API to create an animated gif using images taken between user defined Sols (Solar day on Mars).

## Libs

built with [Compojure](https://github.com/weavejester/compojure), [Ring](https://github.com/ring-clojure/ring) and [Hiccup](https://github.com/weavejester/hiccup)

## Usage

### locally

to run this application locally clone this repository, then run:

```
lein run
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

*Mars exploration*
more about Mars exploration here http://www.nasa.gov/mission_pages/mars/main/index.html
