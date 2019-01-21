include $(OPENPEARL_DIR)/configuration/.config

include $(OPENPEARL_DIR)/runtime/common/Files.common

$(warning $(OPENPEARL_DIR))

COMPONENT_EXTRA_INCLUDES := . $(OPENPEARL_DIR)/runtime/common \
	$(OPENPEARL_DIR)/runtime/FreeRTOS/PEARL 

#COMPONENT_PRIV_INCLUDEDIRS := driver/include driver/include/driver

COMMON_SRCS := $(addprefix common/,$(CXX_COMMON))
COMPONENT_OBJS := $(addsuffix .o,$(basename $(COMMON_SRCS)))

COMPONENT_OBJS += main.o StdOut.o Log.o Esp32Clock.o

$(warning $(COMPONENT_OBJS))

COMPONENT_SRCDIRS := . common
#$(warning $(COMPONENT_SRCDIRS))
#$(warning $(COMPONENT_ADD_INCLUDEDIRS))

#common/TaskCommon.o: CXXFLAGS += -frtti
CXXFLAGS += -frtti
#ifeq ($(CONFIG_ESP32_CHECK_STACK_OVERFLOW),y)
CXXFLAGS += -finstrument-functions
#endif

#COMPONENT_PRIV_INCLUDEDIRS := ../freertos/include/freertos ../freertos/PEARL ../lwip/inlcude/lwip
#COMPONENT_SRCDIRS := . ../freertos/addOns
#COMPONENT_OBJS +=  main.o  \
#	StdOut.o SystemConsole.o I2CBus.o \
#	Esp32Uart.o Esp32UartInternal.o \
#	Esp32Clock.o \
#	Esp32BME280.o bme280.o bme280_wrapper.o \
#	Esp32Wifi.o TcpIpServer.o \
#	 ../freertos/addOns/testStackOverflow.o