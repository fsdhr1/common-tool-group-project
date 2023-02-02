package com.grandtech.echart.util;

import com.grandtech.echart.Geo;
import com.grandtech.echart.Label;
import com.grandtech.echart.Title;
import com.grandtech.echart.Tooltip;
import com.grandtech.echart.axis.CategoryAxis;
import com.grandtech.echart.axis.ValueAxis;
import com.grandtech.echart.code.BrushType;
import com.grandtech.echart.code.CoordinateSystem;
import com.grandtech.echart.code.Roam;
import com.grandtech.echart.code.SeriesType;
import com.grandtech.echart.code.Symbol;
import com.grandtech.echart.code.Trigger;
import com.grandtech.echart.data.MapData;
import com.grandtech.echart.json.GsonOption;
import com.grandtech.echart.series.EffectScatter;
import com.grandtech.echart.series.Line;
import com.grandtech.echart.series.Map;
import com.grandtech.echart.series.Scatter;
import com.grandtech.echart.series.SeriesFactory;
import com.grandtech.echart.series.other.RippleEffect;
import com.grandtech.echart.style.AreaColor;
import com.grandtech.echart.style.AreaStyle;
import com.grandtech.echart.style.ItemStyle;
import com.grandtech.echart.style.Rich;
import com.grandtech.echart.style.TextStyle;
import com.grandtech.echart.style.itemstyle.Emphasis;
import com.grandtech.echart.style.itemstyle.Normal;

/**
 * @ClassName EchartOptionUtil
 * @Description TODO
 * @Author: fs
 * @Date: 2021/12/1 15:00
 * @Version 2.0
 */
public class EchartOptionUtil {
    public static GsonOption getLineChartOptions(Object[] xAxis, Object[] yAxis) {
        GsonOption option = new GsonOption();
        option.title("折线图");
        option.legend("销量");
        option.tooltip().trigger(Trigger.axis);

        ValueAxis valueAxis = new ValueAxis();
        option.yAxis(valueAxis);

        CategoryAxis categorxAxis = new CategoryAxis();
        categorxAxis.axisLine().onZero(false);
        categorxAxis.boundaryGap(true);
        categorxAxis.data(xAxis);
        option.xAxis(categorxAxis);

        Line line = new Line();
        line.smooth(false).name("销量").data(yAxis).itemStyle().normal().lineStyle().shadowColor("rgba(0,0,0,0.4)");
        option.series(line);
        return option;
    }



}
