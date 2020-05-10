package pers.hhz.curtch;

/**
 * gps转百度坐标系
 * @author l
 *
 * WGS-84：是国际标准，GPS坐标（Google Earth使用、或者GPS模块）
    GCJ-02：中国坐标偏移标准，Google
    Map、高德、腾讯使用 BD-09：百度坐标偏移标准，Baidu Map使用

    pi: 圆周率。
    a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
    ee: 椭球的偏心率。
    x_pi: 圆周率转换量。
    transformLat(lat, lon): 转换方法，比较复杂，不必深究了。输入：横纵坐标，输出：转换后的横坐标。
    transformLon(lat, lon): 转换方法，同样复杂，自行脑补吧。输入：横纵坐标，输出：转换后的纵坐标。
    wgs2gcj(lat, lon): WGS坐标转换为GCJ坐标。
    gcj2bd(lat, lon): GCJ坐标转换为百度坐标。
    出现差错的 把 x的 y位置调一下
 *
 */
public class GpsToBaidu {


    //以下是偏移到百度地图的算法
    static double pi = 3.14159265358979324;      //圆周率
    static double ee = 0.00669342162296594323;   //椭球的偏心率
    static double a = 6378245.0;                 //卫星椭球坐标投影到平面地图坐标系的投影因子
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;  //圆周率转换量

    private void GpsToBaidu(){

    }

    // 求弧度
    double radian(double d)
    {
        return d * pi / 180.0;   //角度1? = π / 180
    }


    static double transformLat(double lat, double lon)    //纬度转化
    {
        double ret = -100.0 + 2.0 * lat + 3.0 * lon + 0.2 * lon * lon + 0.1 * lat * lon + 0.2 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lon * pi) + 40.0 * Math.sin(lon / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lon / 12.0 * pi) + 320 * Math.sin(lon * pi  / 30.0)) * 2.0 / 3.0;
        return ret;
    }


    static double transformLon(double lat, double lon)   //经度转化
    {
        double ret = 300.0 + lat + 2.0 * lon + 0.1 * lat * lat + 0.1 * lat * lon + 0.1 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lat / 12.0 * pi) + 300.0 * Math.sin(lat / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }


    /*****************************************************************************
     * WGS84(GPS坐标系) to 火星坐标系(GCJ-02)
     *
     * @param WGS84_Lat
     * @param WGS84_Lon
     * @return
     ****************************************************************************/
    static double[] GPS84_To_GCJ02(double WGS84_Lat, double WGS84_Lon)
    {
        double dLat;
        double dLon;
        double radLat;
        double magic;
        double sqrtMagic;
        double [] GCJ02 = {0.0, 0.0};

        dLat = transformLat(WGS84_Lon - 105.0, WGS84_Lat - 35.0);
        dLon = transformLon(WGS84_Lon - 105.0, WGS84_Lat - 35.0);
        radLat = WGS84_Lat / 180.0 * pi;
        magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        GCJ02[0] = WGS84_Lat + dLat;    //GCJ02_Lat是百度纬度存储变量的地址  *GCJ02_Lat就是那个值
        GCJ02[1] = WGS84_Lon + dLon;    //GCJ02_Lon是百度经度存储变量的地址  *GCJ02_Lon

        return GCJ02;
    }

    /*****************************************
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param GCJ02_Lat
     * @param GCJ02_Lon
     *****************************************/
//传入的参数  	GCJ02_To_BD09(*BD09_Lat,*BD09_Lon,BD09_Lat,BD09_Lon);
//(*BD09_Lat,*BD09_Lon)火星坐标,(BD09_Lat,BD09_Lon)是变量的地址
    static double[] GCJ02_To_BD09(double GCJ02_Lat, double GCJ02_Lon)
    {
        double x = GCJ02_Lon, y = GCJ02_Lat;
        double z= Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta =Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double[] BD09 = {0.0, 0.0};

        BD09[0] = z * Math.cos(theta) + 0.0065;
        BD09[1] = z * Math.sin(theta) + 0.006;

        return BD09;
    }


    /*************************************************************
     函数名称：GPS_transformation(double WGS84_Lat, double WGS84_Lon,double * BD_09_Lat, double * BD_09_Lon)
     函数功能：GPS坐标转百度地图坐标
     输入参数：WGS84_Lat,WGS84_Lon GPS获取到真实经纬度  储存得到的百度经纬度变量的地址 BD_09_Lat,BD_09_Lon指向那个变量
     输出参数：
     *************************************************************/
    public static double[] GPS_transformation(double WGS84_Lat, double WGS84_Lon)
    {
        double[] BD09 = {0.0, 0.0};

        double[] GSJ02 = GPS84_To_GCJ02(WGS84_Lat,WGS84_Lon);                   //GPS坐标转火星坐标
        BD09 = GCJ02_To_BD09(GSJ02[0], GSJ02[1]);                              //火星坐标转百度坐标

        return BD09;
    }

    /*************************************************************
     函数名称：double GetDistance(double lat1, double lng1, double lat2, double lng2)
     函数功能：返回两个点之间的距离
     输入参数：两个点的经纬度(角度)
     输出参数：距离(单位：km)
     *************************************************************/
    double Cal_Distance(double lat1, double lng1, double lat2, double lng2)
    {
        double EARTH_RADIUS = 6378.137;        //地球近似半径
        double radLat1 = radian(lat1);
        double radLat2 = radian(lat2);
        double a = radLat1 - radLat2;
        double b = radian(lng1) - radian(lng2);

        double dst = 2 * Math.asin((Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2) )));    //asin()反正弦值函数

        dst = dst * EARTH_RADIUS;
        dst= (dst * 10000.0) / 10000.0;//舍弃小数点后4位的数
        return dst;
    }
}