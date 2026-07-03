cmake_minimum_required(VERSION 3.10)
project(nauty C)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O4 -mpopcnt -march=native -DUSE_TLS")
set(CMAKE_INSTALL_LIBDIR "${PROJECT_SOURCE_DIR}/../lib")
set(CMAKE_FIND_LIBRARY_PREFIXES "lib" "")
set(CMAKE_FIND_LIBRARY_SUFFIXES ".dll.a" ".a" ".lib" ".so")

find_package(JNI)
if(NOT JNI_FOUND)
	message(FATAL_ERROR "Could not determine Java include path. Please set your JAVA_HOME environment variable to the location of the desired JDK.")
endif()

add_library(nauty SHARED
	"./nauty/naugraph.c"
	"./nauty/naurng.c"
	"./nauty/nausparse.c"
	"./nauty/nautil.c"
	"./nauty/naututil.c"
	"./nauty/nauty.c"
	"./nauty/schreier.c"
	"./core.c"
	"./dev_roanh_cpqindex_Nauty.c"
)

set_target_properties(nauty PROPERTIES
	PROPERTY C_STANDARD 11
	PROPERTY C_STANDARD_REQUIRED ON
	OUTPUT_NAME nauty
)

add_compile_options(-Wall -Wextra -pedantic)

target_include_directories(nauty
	PUBLIC
		$<BUILD_INTERFACE:${JNI_INCLUDE_DIRS}>

	PRIVATE
		${CMAKE_CURRENT_SOURCE_DIR}/include
		${CMAKE_CURRENT_SOURCE_DIR}/include/nauty
)

install(TARGETS nauty LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR} RUNTIME DESTINATION ${CMAKE_INSTALL_LIBDIR})