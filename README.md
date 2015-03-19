# ClientGsu
Android client which offloads tasks via Rest api

Client GSU

Android client - 

1. Install Android SDK for Windows. It includes Eclipse executable file. 

2. Open this Eclipse development environment. Set your Android test device. 

3. Run the application. For the only client-side applications, there is no need to do another thing. Just select a photo from phone gallery and process it locally. 

4. After face detection operation on the client, the faces are drawed with green rectangles. 

Technically, the methods of the built-in framework Android Face Detector is used. 

5. In the server-side offloading operations, the user selects any picture from client and send it to a specified IP to receive the exact coordinates of the pictures detected. After getting the response from the server, the client draws and displays it on UI. 