# SteamgiftsAutoManagerHttp

![tests](https://github.com/przemo199/SteamgiftsAutoManagerHttp/actions/workflows/test.yml/badge.svg)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/przemo199/steamgifts-auto-manager-http)

[DockerHub repository](https://hub.docker.com/repository/docker/przemo199/steamgifts-auto-manager-http)

SteamgiftsAutoManagerHttp is a Java CLI tool designed to automatically scrape and enter giveaways on [steamgifts.com](https://www.steamgifts.com/).  
It is a variation of similar tool called [steamgifts-auto-manager](https://github.com/przemo199/steamgifts-auto-manager) but uses only HTTP requests instead of controlling an instance of Chrome to reduce resource usage and deliver better performance.

## Installation

First, clone this repository:  

```bash
git clone https://github.com/przemo199/SteamgiftsAutoManagerHttp
```

Then, run build:

```bash
gradlew build
```

## Usage

To use this tool you need to correctly set up your requests.txt file.

The first two lines must contain ```cookie``` from your steamgifts.com session and corresponding ```xsrf_token```.

To obtain these values open the browser and go to any giveaway on steamgifts.com that you can enter, open the developer tools and go to the ```Network``` tab, make sure that developer tools are recording network activity and click on ```Enter Giveaway``` or ```Remove Entry``` button, you should see a new request called ```ajax.php```, click on its name and scroll to the very bottom of the ```Headers```  tab, you will find the value of ```cookie``` in the ```Request Headers``` section and the value of ```xsrf_token``` in the ```Form Data``` section.  

The rest of the file should contain desired titles that can be divided into three groups denoted by tags `````[exact_match]````` to enter giveaway if its title matches entirely one of the provided game titles and ```[any_match]``` to enter giveaway if its title contains any of the provided names, additionally none of the giveaways with titles listed below the tag ```[no_match]``` will be entered, title matching in this case is similar to ```[exact_match]``` tag, enter one game title per line and at least one of the ```[exact_match]``` or ```[any_match]``` tags before you launch the tool.

To start the tool use:  

```bash
gradlew run
```
or build or download the docker image from DockerHub and use:

```bash
docker run -d -p <host_port>:8000 -v <host_folder_with_requests_file>:/app/requests steamgifts-auto-manager-http
```
