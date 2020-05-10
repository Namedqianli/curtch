#include "atgm336h.h"

_SaveData Save_Data;

void errorLog(int num)
{
	while (1)
	{
	  	printf("ERROR%d\r\n",num);
	}
}

void parseGpsBuffer()
{
	char *subString;
	char *subStringNext;
	char i = 0;
	if (Save_Data.isGetData == 1)
	{
		Save_Data.isGetData = 0;
		printf("**************\r\n");
		printf(Save_Data.GPS_Buffer);

		
		for (i = 0 ; i <= 6 ; i++)
		{
			if (i == 0)
			{
				if ((subString = strstr(Save_Data.GPS_Buffer, ",")) == NULL)
					errorLog(1);	//解析错误
			}
			else
			{
				subString++;
				if ((subStringNext = strstr(subString, ",")) != NULL)
				{
					char usefullBuffer[2]; 
					switch(i)
					{
						case 1:memcpy(Save_Data.UTCTime, subString, subStringNext - subString);break;	//获取UTC时间
						case 2:memcpy(usefullBuffer, subString, subStringNext - subString);break;		//获取UTC时间
						case 3:memcpy(Save_Data.latitude, subString, subStringNext - subString);break;	//获取纬度信息
						case 4:memcpy(Save_Data.N_S, subString, subStringNext - subString);break;		//获取N/S
						case 5:memcpy(Save_Data.longitude, subString, subStringNext - subString);break;	//获取经度信息
						case 6:memcpy(Save_Data.E_W, subString, subStringNext - subString);break;		//获取E/W

						default:break;
					}

					subString = subStringNext;
					Save_Data.isParseData = 1;
					if(usefullBuffer[0] == 'A')
						Save_Data.isUsefull = 1;
					else if(usefullBuffer[0] == 'V')
						Save_Data.isUsefull = 0;

				}
				else
				{
					errorLog(2);	//解析错误
				}
			}
		}
	}
}
void printGpsBuffer()
{
	if (Save_Data.isParseData == 1)
	{
		Save_Data.isParseData = 0;
		
		printf("Save_Data.UTCTime = ");
		printf(Save_Data.UTCTime);
		printf("\r\n");

		if(Save_Data.isUsefull)
		{
			Save_Data.isUsefull = 0;
			printf("Save_Data.latitude = ");
			printf(Save_Data.latitude);
			printf("\r\n");


			printf("Save_Data.N_S = ");
			printf(Save_Data.N_S);
			printf("\r\n");

			printf("Save_Data.longitude = ");
			printf(Save_Data.longitude);
			printf("\r\n");

			printf("Save_Data.E_W = ");
			printf(Save_Data.E_W);
			printf("\r\n");
		}
		else
		{
			printf("GPS DATA is not usefull!\r\n");
		}
		
	}
}

void clrStruct()
{
	Save_Data.isGetData = 0;
	Save_Data.isParseData = 0;
	Save_Data.isUsefull = 0;
	memset(Save_Data.GPS_Buffer, 0, GPS_Buffer_Length);     //清空
	memset(Save_Data.UTCTime, 0, UTCTime_Length);
	memset(Save_Data.latitude, 0, latitude_Length);			//纬度
	memset(Save_Data.N_S, 0, N_S_Length);
	memset(Save_Data.longitude, 0, longitude_Length);		//经度
	memset(Save_Data.E_W, 0, E_W_Length);
}

void gpsToBaiduMap()
{
	char latitudedd[3] = {0};
    char latitudemm[10] = {0};
    char longtitudedd[4] = {0};
    char longtitudemm[10] = {0};
    int lad = 0, lod = 0;
    double lam = 0.0, lom = 0.0;

    latitudedd[0] = Save_Data.latitude[0];
    latitudedd[1] = Save_Data.latitude[1];
    strcpy(latitudemm, &Save_Data.latitude[2]);
    longtitudedd[0] = Save_Data.longitude[0];
    longtitudedd[1] = Save_Data.longitude[1];
    longtitudedd[2] = Save_Data.longitude[2];
    strcpy(longtitudemm, &Save_Data.longitude[3]);
    if (Save_Data.isParseData){
        sscanf(latitudedd, "%d", &lad);
        sscanf(latitudemm, "%lf", &lam);
        sscanf(longtitudedd, "%d", &lod);
        sscanf(longtitudemm, "%lf", &lom);
        lam = lad + (lam/60.0);
        lom = lod + (lom/60.0);
        memset(Save_Data.latitude, 0, latitude_Length);				//纬度
        memset(Save_Data.longitude, 0, longitude_Length);		    //经度
        sprintf(Save_Data.latitude, "%lf", lam);
        sprintf(Save_Data.longitude, "%lf", lom);
    }
}