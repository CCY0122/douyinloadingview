
## 效果图

安卓版抖音v2.5加载框：

![抖音加载框](https://github.com/CCY0122/douyinloadingview/blob/master/img/1536052594561.gif)

本控件效果图：

![本控件](https://github.com/CCY0122/douyinloadingview/blob/master/img/1536047240533.gif)


## 使用方法


1、xml引用：
```
        <com.douyinloadingview.DYLoadingView
            android:id="@+id/dy3"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#000000"
            app:color1="#FF00EEEE"
            app:color2="#FFFF4040"
            app:....（其他可选属性） /> 
```
2、java使用：
```
	@BindView(R.id.dy1)
    DYLoadingView dy1;

    @OnClick(R.id.b1)
    void start() {
        dy1.setXXXXX(); //设置属性(可选）
        dy1.start(); //开始动画
    }

    @OnClick(R.id.b2)
    void stop() {
        dy1.stop(); //停止动画
    }
```
就酱。


#### 可用属性
| 名称 | 对应xml属性   | 对应java方法| 默认值|
|--------|----------|----------|------|
|球1半径	 | radius1	|setRadius() |6dp  |
|球2半径	 | radius2	|setRadius() |6dp  |
|两球间隔  | gap	|setRadius() |0.8dp  |
|球1颜色	 | color1	|setColors() |0XFFFF4040  |
|球2颜色	 | color2	|setColors() |0XFF00EEEE  |
|叠加色	 | mixColor	|setColors() |0XFF000000  |
|从右往左移动时小球最大缩放倍数	 | rtlScale	|setScales() |0.7f  |
|从左往右移动时小球最大缩放倍数	 | ltrScale	|setScales() |1.3f  |
|一次移动动画时长	 | duration	|setDuration() |350ms  |
|一次移动动画后停顿时长	 | pauseDuration	|setDuration() |80ms  |
|动画进度在[0,scaleStartFraction]期间，小球大小逐渐缩放| scaleStartFraction	|setStartEndFraction() |0.2f  |
|动画进度在[scaleEndFraction,1]期间，小球大小逐渐恢复 | scaleEndFraction	|setStartEndFraction() |0.8f  |

(rtl = right to left, ltr = left to right)

**部分属性说明**：

 - color格式为32位ARGB
 - scaleStartFraction范围[0,0.5]；scaleEndFraction范围[0.5,1]
 - 假设ltrScale = 1.3，scaleStartFraction = 0.2，scaleEndFraction = 0.8；那么实际效果就是一颗小球从左边开始向右移动期间，进度在0%~20%时半径逐渐从1倍放大到1.3倍，在20%~80%期间大小保持1.3倍，在80%~100%时半径逐渐从1.3倍恢复至1倍


## 源码解析
我的博客[Android仿抖音加载框之两颗小球转动控件](https://blog.csdn.net/ccy0122/article/details/82387053)

