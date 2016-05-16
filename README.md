# 3D Controller

This Android Experiment turns your phone into a 3D controller. It detects the device's orientation in 3D space (where it's pointing and how it's rotated) and makes that information accessible through a simple API.

In this app you will find 3 demos that utilize this API. The first applies your device's orientation to a 3D model. The second shows the wide range of orientations the API can detect and the third turns your device into a laser pointer simulator.

#Get the demo
You can download the demo app [here](https://play.google.com/store/apps/details?id=com.geomme.controller).

Alternetavley you can download the source code and run the app in developer mode on your chromecast device.  
don't forget to [register you applicatoin](https://developers.google.com/cast/docs/registration) and to replace the placeholder in `strings.xml` with your own chromecast app id.
```
<string name="app_id">YOUR_CHROME_CAST_APP_ID_HERE</string>
```

# How to use
Note that this code was written in a relativly short time as an experiment and not as a library. Notably the laser pointer code can be improved to take into account the size of the TV and the distance between the user and the TV. Also there is no particular limitation to the number of orientations that can be detected, we just chose to detect the most useful ones. 

All the controller code is in [this package](https://github.com/hatemsh/3D-controller/tree/master/app/src/main/java/com/example/controller/controller).

## Example
This is an example of an activity that uses the API to get data from the sensors.

```
MatrixCalculator mMatrixCalculator;
ControllerSensorManager mControllerSensorManager;
float mLaserX, mLaserY, mWidth, mHeight;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMatrixCalculator = new MatrixCalculator();
    
    //the activity implements MatrixCalculator.OrientationListener
    mMatrixCalculator.registerOrientationListener(this);
    
    mControllerSensorManager = new ControllerSensorManager(this, mMatrixCalculator);
    mControllerSensorManager.start(); 
}
```

Don't forget to stop the ControllerSensorManager in `onStop()`. 
```
@Override
protected void onStop() {
    super.onStop();
    mControllerSensorManager.stop();
}
```

You should call mMatrixCalculator.offset() after telling the user to hold the phone in it's default position.
```
public void onShouldOffsetMatrix(){
    mMatrixCalculator.offset();
}
```


`newOrientation` will be one of 16 detectable orientations or `ORIENTATION_UNKNOWN` if not detected.
The orientation constants describe which face of the phone the user is seeing and where the phone is pointing,
ranging from `ORIENTATION_FRONT_TOP` to `ORIENTATION_BACK_TOP_LEFT`.  

*This is all relative to the position the phone was held in when `mMatrixCalculator.offset()` was called*
```
@Override
public void onOrientationChanged(int newOrientation) {
    switch (newOrientation){
        case OrientationDetector.ORIENTATION_FRONT_TOP:
            // the user is seeing the front of the phone
            // the top of the phone is pointing up
            break;
        case OrientationDetector.ORIENTATION_BACK_TOP_LEFT:
            // the user is seeing the back of the phone
            // the top of the phone is pointing to the top left (north west)
            break;
    }
}
```


Detect where the top of the phone is pointing (x, y coordinates)
*Again, relative to the position the pohne was held in when mMatrixCalculator.offset() was called*
```
@Override
public void onLaserPointChanged(float[] point) {
    // the width and height (in pixels) of the screen being pointed at
    OrientationDetector.scaleLaserPoint(point, mWidth, mHeight);
    mLaserX = point[0];
    mLaserY = point[1];
}
```

**How to get the rotation matrix**  

This retuned matrix is what we use to rotate the chair 
```
mMatrixCalculator.getRotationMatrix();
```

#License
Apache License, Version 2.0. See the [LICENSE](https://github.com/hatemsh/3D-controller/blob/master/LICENSE.md) file for details.

