package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.UPath;

import java.util.Collections;
import java.util.Map;

/**
 * Created by michal on 21/07/16.
 */
public class X_Hw extends XPRZ_SectionController
{
    protected Map<String,OBPhoneme> componentDict;
    protected OBControl drawRect, board,eraser, eraser2,arrowButton;
    protected OBPath  lineTop,lineBottom;
    protected XPRZ_Presenter presenter;

    PointF startPoint;
    OBPath drawPath;
    UPath uPathDraw;
    boolean eraserMode, arrowButtonHidden;
    Canvas bitmapContext;
    OBControl drawOn;
    //UIImage eraserUIImage;
   // CGLayerRef eraserLayerRef;



    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadFingers();
        loadEvent("master");

        board = objectDict.get("board");
        board.setFrame(board.frame());
        board.setPosition(new PointF((int)board.position().x, (int)board.position().y));
        lineTop = (OBPath)objectDict.get("line_top");
        lineBottom = (OBPath)objectDict.get("line_bottom");
        arrowButton = objectDict.get("arrow_next");

        board.setZPosition(1);
        objectDict.get("frame").setZPosition(30);
        lineTop.setZPosition(2);
        lineBottom.setZPosition(2);

        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", XPRZ_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);

        Map<String,Object> ed = loadXML(getConfigPath("tracingletters.xml"));
        eventsDict.putAll(ed);

        drawOn = new OBControl();
       // drawOn.setFrame( CGRectMake(0, 0, board.width*[UIScreen.mainScreen()scale], board.height*.get(UIScreen.mainScreen()scale)));
        drawOn.setPosition(board.position());
        drawOn.setZPosition(10);
       // drawOn.setScale( 1/.get(UIScreen.mainScreen()scale));
        attachControl(drawOn);

        componentDict = OBUtils.LoadWordComponentsXML(true);

        arrowButton.show();
        arrowButton.highlight();
        arrowButton.lowlight();
        arrowButton.hide();

        drawRect = new OBControl();
        RectF rect = new RectF(board.frame());
        rect.inset(-applyGraphicScale(20), -applyGraphicScale(20));
        drawRect.setFrame(rect);
        drawRect.setZPosition(2);
        drawRect.hide();
        attachControl(drawRect);

        eraser = objectDict.get("eraser");
        OBControl eraser2old = objectDict.get("eraser_2");

        eraser2old.show();
        OBControl copy = eraser2old.copy();
        attachControl(copy);
        eraser2 = new OBGroup(Collections.singletonList(copy));
        // eraserUIImage = .get(eraser2 renderedImage);
        attachControl(eraser2);

        eraser2old.hide();
        eraser2.setZPosition(20);
        eraser2.hide();

        arrowButtonHidden = true;
    }


}
