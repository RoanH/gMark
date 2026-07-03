mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=RELEASE -G "MinGW Makefiles" ..
mingw32-make install