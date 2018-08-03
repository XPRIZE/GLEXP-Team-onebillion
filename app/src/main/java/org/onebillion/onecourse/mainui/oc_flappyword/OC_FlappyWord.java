package org.onebillion.onecourse.mainui.oc_flappyword;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_meetletter.OC_LetterBox;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 10/04/2017.
 */

public class OC_FlappyWord extends OC_SectionController
{
    float GRAVITY, FLAP_DURATION, FLAP_GRAVITY, SCROLL_ACCELERATION, INITIAL_SCROLL_SPEED;
    OBGroup bord, bordBody;
    List<Integer> wordColours;
    List<List<OBControl>> bordFrames;
    List<OBPhoneme> eventTargets;
    float bordGravity, screenScrollSpeed, treeDistance, cloudDistance;
    long lastFlapTime,fallStartTime, lastFrameTime;
    List<Integer> flapAnimationFrames;
    List<OBControl> scrollTrees, scrollClouds;
    List<OBLabel> scrollLetters;
    List<List<OBControl>> scrollLandscapes;
    int currentBordFrame,currentWordColourIndex;
    float lastFlapPositionY, lastFlapRotation, lastFallRotation;
    boolean animate, currentWordCompleted;
    float groundLevel, skyLevel;
    List<OBControl> treeSelection;
    List<OBLabel> lettersSelection;
    List<OBControl> cloudSelection, randomTreeSelection;
    List<List<OBControl>> landscapeSelection;
    OBControl hitbox;
    Map<String,OBPhoneme> componentDict;
    int letterAppearCountdown, currentLetterIndex, currentScore, currentTreeIndex, currentWordIndex;
    OC_LetterBox letterBox;
    OBLabel letterBoxCounter, bigWordLabel;
    OBControl nest;
    boolean letterMode;

    public void prepare()
    {
        componentDict = OBUtils.LoadWordComponentsXML(true);
        GRAVITY = applyGraphicScale(1000);
        SCROLL_ACCELERATION = applyGraphicScale(0.1f);
        FLAP_DURATION = 0.3f;
        FLAP_GRAVITY = GRAVITY *0.5f;
        INITIAL_SCROLL_SPEED = applyGraphicScale(2);
        treeDistance = randomTreeDistance();
        screenScrollSpeed = INITIAL_SCROLL_SPEED;
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        letterMode = parameters.get("mode").equals("letter");
        //load box
        OBGroup box = (OBGroup)objectDict.get("box_number");
        box.setRasterScale(box.scale()*0.5f);
        letterBox = new OC_LetterBox(box, this);
        OBControl numbox = box.objectDict.get("numbox");
        RectF labelRect = numbox.getWorldFrame();
        float fontSize = 70.0f * labelRect.height()/80.0f;
        letterBox.control.setZPosition(40);
        letterBoxCounter = new OBLabel("888",OBUtils.standardTypeFace(),fontSize);
        letterBoxCounter.setPosition(numbox.getWorldPosition());
        letterBoxCounter.setColour(Color.BLACK);
        attachControl(letterBoxCounter);
        letterBoxCounter.setZPosition(box.zPosition()+1);
        setCurrentScore(0);
        //load bord    hideControls("bord_.*");
        bord = (OBGroup)objectDict.get("bord_2");
        bord.setProperty("start_loc", OBMisc.copyPoint(bord.position()));
        bord.hideMembers("wing_.*");
        bord.setShouldTexturise(false);
        bord.objectDict.get("body").setShouldTexturise(false);
        bord.setRasterScale(bord.scale()*0.7f);
        bordFrames = new ArrayList<>();
        bordFrames.add(bord.filterMembers("wing_.*0"));
        bordFrames.add(bord.filterMembers("wing_.*1"));
        bordFrames.add(bord.filterMembers("wing_.*2"));
        bordFrames.add(bord.filterMembers("wing_.*3"));
        bordFrames.add(bord.filterMembers("wing_.*4"));
        bordBody = (OBGroup)bord.objectDict.get("body");
        bordBody.setAnchorPoint(OB_Maths.relativePointInRectForLocation(bord.objectDict.get("joint").position(), bordBody.frame()));
        showBordFrame(3);
        flapAnimationFrames = Arrays.asList(4,3,2,1,2,3,4);
        lastFlapTime = 0;
        lastFlapRotation = lastFallRotation = 0;
        //load moving elemenets
        groundLevel = objectDict.get("bottom_bar").top();
        skyLevel = -applyGraphicScale(50);
        cloudSelection = filterControls("cloud_.*");
        scrollTrees = new ArrayList<>();
        scrollClouds = new ArrayList<>();
        scrollLandscapes = new ArrayList<>();
        scrollLetters = new ArrayList<>();
        lettersSelection = new ArrayList<>();
        treeSelection = new ArrayList<>();
        landscapeSelection = new ArrayList<>();
        prepareRandomTrees();
        for(int i=0; i<4; i++)
        {
            List<OBControl> landscapes = new ArrayList<>();
            for(OBControl landscape : filterControls(String.format("landscape_%d_.*",i)))
            {
                landscape.show();
                OBControl renderedImage = landscape.copy();
                renderedImage.setRasterScale(i==0 ? renderedImage.scale() :renderedImage.scale()*0.5f);
                renderedImage.setLeft(bounds().width());
                landscape.hide();
                landscapes.add(renderedImage);
            }
            landscapeSelection.add(landscapes);
            scrollLandscapes.add(new ArrayList<OBControl>());
        }
        bord.setZPosition ( 20);
        bord.setProperty("start_loc",OBMisc.copyPoint(bord.position()));
        //prepare landscape
        addRandomLandscape();
        for(int i=0; i<landscapeSelection.size(); i++)
            scrollLandscapes.get(i).get(0).setLeft(0);

        while(scrollLandscapes.get(0).get(scrollLandscapes.get(0).size()-1).right() < bounds().width())
            addRandomLandscape();
        //prepare clouds
        addRandomCloudLeft(bounds().width() * 0.5f * (float)OB_Maths.rndom());
        addRandomCloudLeft(bounds().width() * (0.5f +(0.5f * (float)OB_Maths.rndom())));
        nest = objectDict.get("nest");
        nest.setZPosition(30);
        nest.setAnchorPoint(1,1);
        nest.setProperty("dest_loc",OBMisc.copyPoint(nest.position()));
        nest.setRasterScale(nest.scale()*0.7f);
        bordBody.setRotation((float)Math.toRadians(-30));
        showBordFrame(0);
        bord.setPosition(OB_Maths.locationForRect(0.5f,-0.1f,nest.frame()));
        bord.hide();
        eventTargets = new ArrayList<>();
        String[] wordList = parameters.get("words").split(",");
        for(String phoid : wordList)
        {
            if(componentDict.get(phoid) != null)
                eventTargets.add(componentDict.get(phoid));
        }
        currentWordIndex =0;
        List<Integer> wColours = new ArrayList<>();
        for(String colourString : eventAttributes.get("word_colours").split(";"))
            wColours.add(OBUtils.colorFromRGBString(colourString));
        wordColours = OBUtils.randomlySortedArray(wColours);
        currentWordColourIndex = 0;
        ((OBPath)objectDict.get("line")).sizeToBoundingBoxIncludingStroke();
        loadTarget(currentWordIndex,letterMode);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                animateBordLand();
                setStatus(STATUS_AWAITING_CLICK);
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX()
    {

    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            setStatus(STATUS_WAITING_FOR_DRAG);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    startBordFlight();
                    startGameLoop();

                }

            });
        }
        else if(status() == STATUS_WAITING_FOR_DRAG)
        {
            triggerBordFlap();

        }

    }


    public void startBordFlight()
    {
        currentWordCompleted = false;
        lastFallRotation = 0;
        lastFlapRotation = (float)Math.toDegrees(bordBody.rotation);
        bord.objectDict.get("legs").hide();
        triggerBordFlap();
    }

    public void prepareRandomTrees()
    {
        treeSelection.clear();
        List<Integer> treeColours = new ArrayList<>();
        for(String colourString : eventAttributes.get("tree_colours").split(";"))
            treeColours.add(OBUtils.colorFromRGBString(colourString));
        List<Integer> randomIndexes = OBUtils.RandomIndexesTo((int)treeColours.size());
        List<Float> treeScales = Arrays.asList(0.95f, 1.0f, 1.05f, 1.1f);
        int colourIndex = 0;
        for(OBControl tree : filterControls("tree_.*"))
        {
            tree.show();
            for(int i=0; i<4; i++)
            {
                int colour = treeColours.get(randomIndexes.get(colourIndex));
                for(OBControl path : ((OBGroup)tree).filterMembers("colour.*"))
                {
                    ((OBPath)path).setFillColor(colour);

                }
                OBControl renderedImage = tree.copy();
                // renderedImage.setScale *( (float)treeScales.get(i) );
                treeSelection.add(renderedImage);
                //renderedImage.setRasterScale(0.5f);
                renderedImage.setLeft ( bounds().width());
                renderedImage.setRasterScale(0.5f*renderedImage.scale());
                renderedImage.setAnchorPoint(new PointF(1,1));
                colourIndex++;
                if(colourIndex >= randomIndexes.size())
                {
                    colourIndex = 0;
                    randomIndexes = OBUtils.RandomIndexesTo((int)treeColours.size());
                }
            }
            tree.hide();

        }
        randomTreeSelection = OBUtils.randomlySortedArray(treeSelection);

    }
    public float randomTreeDistance()
    {
        return (0.2f + 0.4f * (float)OB_Maths.rndom()) * bounds().width();

    }
    public float randomCloudDistance()
    {
        return ((0.1f) + 0.2f * (float)OB_Maths.rndom()) * bounds().width();

    }


    public void triggerBordFlap()
    {
        lastFlapPositionY = bord.position().y;
        lastFlapTime =fallStartTime= SystemClock.uptimeMillis();
        bordGravity = FLAP_GRAVITY;
    }

    public void showBordFrame(int index)
    {
        if(currentBordFrame == index)
            return;
        for(List<OBControl> frames : bordFrames)
        {
            for(OBControl frame : frames)
            {
                if(frames == bordFrames.get(index))
                {
                    frame.show();
                }
                else
                {
                    frame.hide();
                }
            }
        }
        currentBordFrame = index;
    }

    public void scrollLoop()
    {
        if(this._aborting)
            stopGameLoop();
        boolean finishScrollLoop = false;
        float scrollSpeed = INITIAL_SCROLL_SPEED * 2;
        long currentTime = SystemClock.uptimeMillis();
        float frameFrac = (currentTime - lastFrameTime)/5.0f ;
        processTreesAndLetters(frameFrac, false,scrollSpeed);
        processClouds(frameFrac,scrollSpeed);
        processLandscape(frameFrac,scrollSpeed);
        PointF destLoc = (PointF)nest.propertyValue("dest_loc") ;
        float newNestX = nest.position().x - scrollSpeed * frameFrac;
        if(newNestX  > destLoc.x)
        {
            nest.setPosition(newNestX, nest.position().y);
        }
        else
        {
            nest.setPosition(destLoc);
            finishScrollLoop = true;

        }
        lastFrameTime = currentTime;
        if(finishScrollLoop)
        {
            stopScrollLoop();
            scrollLoopFinished();
        }
    }


    public void gameLoop()
    {
        if(this._aborting)
            stopGameLoop();

        long currentTime = SystemClock.uptimeMillis();

        float frameFrac = (currentTime - lastFrameTime)/5.0f ;
        processTreesAndLetters(frameFrac,!currentWordCompleted,screenScrollSpeed);
        processClouds(frameFrac,screenScrollSpeed);
        processLandscape(frameFrac,screenScrollSpeed);
        if(nest.position().x > 0)
        {
            nest.setPosition(nest.position().x - screenScrollSpeed * frameFrac,nest.position().y);

        }
        else
        {
            nest.hide();
        }
        boolean stopGame = false;
        if(!currentWordCompleted)
        {
            float frac = OB_Maths.clamp01((currentTime-lastFlapTime)/(FLAP_DURATION*1000));
            int index = 0;
            if(frac >= 1)
                index = (int)flapAnimationFrames.size()-1;
            else
                index = (int)Math.floor(frac*flapAnimationFrames.size());
            showBordFrame(flapAnimationFrames.get(index));
            PointF loc = OBMisc.copyPoint(bord.position());

            float flapTime =  (currentTime - fallStartTime)/1000.0f;
            float distance = bordGravity * flapTime - 0.5f*GRAVITY*flapTime*flapTime;
            float currentSpeed = bordGravity - flapTime*GRAVITY;

            float currentLocY = lastFlapPositionY - distance;
            if(currentLocY >= groundLevel)
            {
                loc.y = groundLevel;
                bordGravity =0;
                stopGame = true;

            }
            else
            {
                loc.y = currentLocY;

            }
            bord.setPosition(loc);
            if(bord.top() <= skyLevel)
            {
                bord.setTop(skyLevel);
                bordGravity =0;
                lastFlapPositionY = bord.position().y;
                fallStartTime = currentTime;

            }
            if(!stopGame)
            {
                if(currentSpeed <= 0)
                {
                    lastFallRotation = lastFlapRotation + (35-lastFlapRotation) * OB_Maths.clamp01((-1*currentSpeed/(FLAP_GRAVITY)));
                    bordBody.setRotation((float)Math.toRadians(lastFallRotation));

                }
                else
                {
                    float currentRotation = lastFallRotation + (-35-lastFallRotation) * OB_Maths.clamp01(frac);
                    if(lastFlapRotation > currentRotation)
                    {
                        lastFlapRotation = currentRotation;

                    }
                    bordBody.setRotation((float)Math.toRadians(lastFlapRotation));

                }

            }
            if(checkTreeCollision())
            {
                stopGame = true;
            }
            checkLetterCollision();
        }
        if(stopGame)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    stopGameLoop();
                    gameLost();

                }

            });
        }
        lastFrameTime = currentTime;
        if(currentWordCompleted && scrollTrees.size() == 0)
        {
            stopGameLoop();
            wordCompleted();

        }

    }


    public void processTreesAndLetters(float frac,boolean addTrees, float scrollSpeed)
    {
        if(addTrees)
        {
            boolean add = true;
            for(OBControl tree : scrollTrees)
            {
                if(bounds().width() < tree.position().x + treeDistance)
                {
                    add = false;
                    break;
                }

            }
            if(add)
            {
                OBControl treeImage = randomTreeSelection.get(currentTreeIndex);
                currentTreeIndex++;
                if(currentTreeIndex >= randomTreeSelection.size())
                {
                    currentTreeIndex = 0;
                    randomTreeSelection = OBUtils.randomlySortedArray(treeSelection);

                }
                OBControl copiedImage = treeImage.copy();
                copiedImage.setBottom ( OB_Maths.locationForRect(0f,0.2f,objectDict.get("bottom_bar").frame()).y);
                copiedImage.setAnchorPoint(new PointF(1,1));
                attachControl(copiedImage);
                copiedImage.show();
                scrollTrees.add(copiedImage);
                treeDistance = randomTreeDistance();
                letterAppearCountdown--;
                if(letterAppearCountdown <= 0 && scrollLetters.size() == 0)
                {
                    if(currentLetterIndex < lettersSelection.size())
                    {
                        OBLabel currentLetter = lettersSelection.get(currentLetterIndex);
                        OBLabel bigLetter = (OBLabel)currentLetter.propertyValue("big_label");
                        bigLetter.setPosition(OB_Maths.locationForRect(0.5f, 0.5f, copiedImage.frame()));
                        bigLetter.setBottom(copiedImage.top() - applyGraphicScale(20));
                        bigLetter.show();
                        scrollLetters.add(bigLetter);
                    }
                }
            }
        }
        List<OBControl> toRemove = new ArrayList<>();
        for(OBControl tree : scrollTrees)
        {
            tree.setPosition(tree.position().x -  scrollSpeed * frac, tree.position().y);
            if(tree.position().x < 0)
                toRemove.add(tree);

        }
        for(OBControl removeControl : toRemove)
        {
            detachControl(removeControl);
            scrollTrees.remove(removeControl);
        }

        toRemove.clear();

        for(OBControl letter : scrollLetters)
        {
            letter.setPosition(letter.position().x -  scrollSpeed * frac, letter.position().y);
            if(letter.right() < 0)
                toRemove.add(letter);
        }

        for(OBControl removeControl : toRemove)
        {
            scrollLetters.remove(removeControl);
        }

        if(toRemove.size() > 0)
        {
            letterAppearCountdown = OB_Maths.randomInt(0, 3);
        }
    }

    public void processClouds(float frac, float scrollSpeed)
    {
        if(addRandomCloudLeft(bounds().width()))
        {
            cloudDistance = randomCloudDistance();

        }
        List<OBControl> toRemove = new ArrayList<>();
        for(OBControl cloud : scrollClouds)
        {
            cloud.setPosition(cloud.position().x -  scrollSpeed * frac * (float)cloud.propertyValue("speed"), cloud.position().y);
            if(cloud.position().x < 0)
                toRemove.add(cloud);

        }
        for(OBControl removeControl : toRemove)
        {
            detachControl(removeControl);
            scrollClouds.remove(removeControl);
        }
    }

    public boolean addRandomCloudLeft(float left)
    {
        if(scrollClouds.size() == 0 || bounds().width() > scrollClouds.get(scrollClouds.size()-1).right() + cloudDistance)
        {
            OBControl cloudImage = cloudSelection.get(OB_Maths.randomInt(0,cloudSelection.size()-1));
            OBControl copiedImage = cloudImage.copy();
            copiedImage.setScale(0.5f + 0.5f * (float)OB_Maths.rndom());
            copiedImage.setRasterScale(copiedImage.scale());
            copiedImage.setTop((-0.05f + (0.25f * (float)OB_Maths.rndom())) * bounds().height());
            copiedImage.setLeft ( left);
            if(OB_Maths.randomInt(0, 1) == 1)
            {
                copiedImage.flipHoriz();
                copiedImage.setAnchorPoint(new PointF(0,1));
            }
            else
            {
                copiedImage.setAnchorPoint(new PointF(1,1));
            }
            copiedImage.setProperty("speed",0.2f + (0.2f*(float)OB_Maths.rndom()));
            attachControl(copiedImage);

            copiedImage.show();
            scrollClouds.add(copiedImage);
            return true;
        }
        return false;
    }

    public void processLandscape(float frac, float scrollSpeed)
    {
        addRandomLandscape();
        for(int i=0; i< scrollLandscapes.size(); i++)
        {
            List<OBControl> toRemove = new ArrayList<>();
            for(OBControl landscape : scrollLandscapes.get(i))
            {
                landscape.setPosition(landscape.position().x -scrollSpeed * frac * (i==0?1:(0.37f - (i*0.07f))), landscape.position().y);
               if(landscape.position().x < 0)
                    toRemove.add(landscape);

            }

            for(OBControl removeControl : toRemove)
            {
                detachControl(removeControl);
                scrollLandscapes.get(i).remove(removeControl);
            }
        }
    }

    public void addRandomLandscape()
    {
        for(int i=0; i< landscapeSelection.size(); i++)
        {
            if(scrollLandscapes.get(i).size() == 0 || scrollLandscapes.get(i).get(scrollLandscapes.get(i).size()-1).right() < 1.2*bounds().width())
            {
                OBControl landscapeImage = landscapeSelection.get(i).get(OB_Maths.randomInt(0, landscapeSelection.get(i).size()-1));
                OBControl copiedImage = landscapeImage.copy();
                if(scrollLandscapes.get(i).size() != 0)
                    copiedImage.setLeft(scrollLandscapes.get(i).get(scrollLandscapes.get(i).size()-1).right() - applyGraphicScale(1));
                else
                    copiedImage.setLeft(0);

                copiedImage.setAnchorPoint(new PointF(1,1));
                //copiedImage.setBottom ( OB_Maths.locationForRect(0f,0.05f,objectDict.get("bottom_bar_2").frame()).y);
                attachControl(copiedImage);
                copiedImage.show();
                scrollLandscapes.get(i).add(copiedImage);
            }
        }
    }

    public boolean checkTreeCollision()
    {
        boolean collisionDetected = false;
        for(OBControl tree : scrollTrees)
        {
            //hitbox.setPosition ( bord.position);
            if(tree.intersectsWithn(bord))
            {
                collisionDetected = true;
                break;
            }
        }
        return collisionDetected;
    }


    public void checkLetterCollision()
    {
        OBLabel collidedLetter = null;
        for(OBLabel letter : scrollLetters)
        {
            //hitbox.setPosition ( bord.position);
            if(letter.intersectsWithn(bord))
            {
                collidedLetter = letter;
                break;
            }
        }
        if(collidedLetter != null)
        {
            scrollLetters.remove(collidedLetter);
            currentLetterIndex++;
            if(currentLetterIndex >= lettersSelection.size())
            {
                currentWordCompleted = true;
            }
            flyBigLetterToParent(collidedLetter, currentWordCompleted);
        }
    }

    public void flyBigLetterToParent(final OBLabel bigLabel, final boolean withBord)
    {
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                OBPhoneme pho = (OBPhoneme)bigLabel.propertyValue("phoneme");
                pho.playAudio(controller,false);
                OBLabel smallLabel = (OBLabel)bigLabel.propertyValue("small_label");
                List<OBAnim> anims = new ArrayList<>();
                anims.add(OBAnim.moveAnim(smallLabel.position(), bigLabel));
                anims.add(OBAnim.rotationAnim((float)Math.toRadians(OB_Maths.randomInt(2, 4) * 360), bigLabel));
                anims.add(OBAnim.scaleAnim(smallLabel.width() / bigLabel.width(), bigLabel));
                if (withBord)
                {
                    anims.add(bordFlapAnim(10,false));
                    anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f, -0.1f, bounds()), bord));
                }
                OBAnimationGroup.runAnims(anims, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, controller);
                smallLabel.show();
                bigLabel.hide();

            }
        });
    }

    public OBAnim bordFlapAnim(final int count,final boolean land)
    {
        final List<Integer> bordAnim  = Arrays.asList(4,3,2,1,2,3,4);
        OBAnim flapAnimation = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float currentFrac = frac * count;
                showBordFrame(bordAnim.get((int) Math.round((bordAnim.size() - 1) * currentFrac) % bordAnim.size()));
                bordBody.setRotation(land ? (float) Math.toRadians(OB_Maths.clamp01((frac - 0.5f) / 0.5f) * -30) : (float) Math.toRadians((1 - OB_Maths.clamp01((frac) / 0.5f)) * -30));
            }
        };

        return flapAnimation;

    }
    public void animateBordLand()
    {
        bord.objectDict.get("legs").hide();
        bord.setPosition ( OB_Maths.locationForRect(-0.1f,-0.1f,this.bounds()));
        bord.show();
        OBAnim flapAnimation = bordFlapAnim(10,true);
        bordBody.setRotation ( 0);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)bord.propertyValue("start_loc"),bord), flapAnimation)
                ,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        bord.objectDict.get("legs").show();
        showBordFrame(0);
        unlockScreen();
    }

    public void startGameLoop()
    {
        animate = true;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                gameLoop();
            }
        };
        OBAnimationGroup ag = new OBAnimationGroup();
        lastFrameTime = SystemClock.uptimeMillis();
        ag.applyAnimations(Arrays.asList(blockAnim), 100, false, OBAnim.ANIM_LINEAR, -1, null, this);
        registerAnimationGroup(ag,"gameLoop");
    }

    public void stopGameLoop()
    {
        deregisterAnimationGroupWithName("gameLoop");
    }

    public void startScrollLoop()
    {
        animate = true;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                scrollLoop();
            }
        };
        OBAnimationGroup ag = new OBAnimationGroup();
        lastFrameTime = SystemClock.uptimeMillis();
        ag.applyAnimations(Arrays.asList(blockAnim), 100, false, OBAnim.ANIM_LINEAR, -1, null, this);
        registerAnimationGroup(ag,"scrollLoop");

    }

    public void stopScrollLoop()
    {
        deregisterAnimationGroupWithName("scrollLoop");

    }

    public void scrollLoopFinished()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                lockScreen();
                currentWordIndex++;
                if (currentWordIndex >= eventTargets.size())
                    currentWordIndex = 0;
                loadTarget(currentWordIndex, letterMode);
                prepareRandomTrees();

                unlockScreen();
                animateBordLand();
                setStatus(STATUS_AWAITING_CLICK);
            }
        });
    }

    public void loadTarget(int targetIndex, boolean letterMode)
    {
        if(targetIndex == 0)
            eventTargets = OBUtils.randomlySortedArray(eventTargets);
        OBPhoneme target = eventTargets.get(targetIndex);
        for(OBControl letter : lettersSelection)
        {
            detachControl((OBControl)letter.propertyValue("line"));
            detachControl((OBControl)letter.propertyValue("big_label"));
            detachControl(letter);
        }
        lettersSelection.clear();
        if(bigWordLabel != null)
        {
            detachControl(bigWordLabel);
        }
        bigWordLabel = null;
        int wordColour = wordColours.get(currentWordColourIndex);
        currentWordColourIndex++;
        if(currentWordColourIndex >= wordColours.size())
        {
            wordColours = OBUtils.randomlySortedArray(wordColours);
            currentWordColourIndex = 0;
        }
        letterAppearCountdown = OB_Maths.randomInt(3, 6);
        currentLetterIndex = currentTreeIndex = 0;
        OBControl lineControl = objectDict.get("line");
        List<OBLabel> smallParts = new ArrayList<>();
        OBControl smallTextBox = objectDict.get("word_rect_small");
        OBControl bigTextBox = objectDict.get("word_rect_big");
        float smallFontSize = 55.0f * smallTextBox.height()/80.0f;
        float bigFontSize = 220.0f * smallTextBox.height()/125.0f;
        float smallWordScale = 1;
        OBLabel smallWordLabel = new OBLabel(target.text, OBUtils.standardTypeFace(), smallFontSize);
        smallWordLabel.setPosition(smallTextBox.position());
        //attachControl(fullWordLabel);
        if(smallWordLabel.width()>smallTextBox.width())
            smallWordScale = 1.0f -((smallWordLabel.width()-smallTextBox.width())*1.0f/smallWordLabel.width());
        bigWordLabel = new OBLabel(target.text,OBUtils.standardTypeFace(),bigFontSize);

        float bigWordScale = 1;
        //attachControl(fullWordLabel);
        if(bigWordLabel.width() >smallTextBox.width())
            bigWordScale = 1.0f -((bigWordLabel.width()-smallTextBox.width())*1.0f/bigWordLabel.width());

        bigWordLabel.setProperty("phoneme",target);
        bigWordLabel.setProperty("dest_scale",bigWordScale);
        bigWordLabel.setProperty("dest_loc",OBMisc.copyPoint(bigTextBox.position()));
        attachControl(bigWordLabel);
        bigWordLabel.setReversedScreenMaskControl(letterBox.mask);
        bigWordLabel.setZPosition(50);
        bigWordLabel.setPosition(smallTextBox.position());

        List<OBPhoneme> parts = null;
        if(!letterMode && target instanceof OBWord)
        {
            parts = (List<OBPhoneme>) (Object) ((OBWord) target).syllables();
        }
        else
        {
            parts = new ArrayList<>();
            for(int i=0; i<target.text.length(); i++)
            {
                String textPart = target.text.substring(i,i+1);
                parts.add(new OBPhoneme(textPart,String.format("alph_%s",textPart)));
            }
        }
        int searchStart =0;
        float maxLabelWidth = -1;
        for(int i=0; i<parts.size(); i++)
        {
            OBPhoneme partPhoeneme = parts.get(i);
            int rangeStart = target.text.indexOf(partPhoeneme.text,searchStart);
            if(rangeStart != -1)
            {
                searchStart += partPhoeneme.text.length();
                RectF bb = OBUtils.getBoundsForSelectionInLabel(rangeStart,rangeStart+partPhoeneme.text.length(),smallWordLabel);

                float left = bb.left;
                OBLabel partLabel = new OBLabel(partPhoeneme.text,OBUtils.standardTypeFace(),smallFontSize);
                partLabel.setColour(wordColour);
                partLabel.setPosition(smallTextBox.position());
                partLabel.setScale(smallWordScale);
                partLabel.setZPosition(20);
                partLabel.setLeft(left);
                partLabel.setProperty("word_loc", OBMisc.copyPoint(partLabel.position()));
                partLabel.hide();
                smallParts.add(partLabel);
                OBLabel bigPartLabel = new OBLabel(partPhoeneme.text,OBUtils.standardTypeFace(),bigFontSize);
                bigPartLabel.setColour(wordColour);
                bigPartLabel.setZPosition(20);
                bigPartLabel.setPosition(bigTextBox.position());
                bigPartLabel.setRasterScale(0.5f);
                bigPartLabel.hide();
                partLabel.setProperty("big_label",bigPartLabel);
                bigPartLabel.setProperty("phoneme",partPhoeneme);
                bigPartLabel.setProperty("small_label",partLabel);
                attachControl(bigPartLabel);
                attachControl(partLabel);
                lettersSelection.add(partLabel);

                if(maxLabelWidth < partLabel.width())
                    maxLabelWidth = partLabel.width();
            }
        }
        bigWordLabel.setScale(lettersSelection.get(0).height()/bigWordLabel.height());
        bigWordLabel.hide();
        float gapSize = applyGraphicScale(10);
        float lineLength = maxLabelWidth;
        PointF midLoc = OB_Maths.locationForRect(0.5f,0.5f,bigWordLabel.frame());
        float startLeft =  midLoc.x - (lineLength * lettersSelection.size() + gapSize * (lettersSelection.size()-1))/2.0f;
        float right = 0;
        for(int i = 0; i < lettersSelection.size(); i++)
        {
            OBLabel partLabel = lettersSelection.get(i);
            partLabel.setPosition (startLeft + (gapSize * (i-1)) + (lineLength)*(i+0.5f), midLoc.y);

            partLabel.setProperty("drop_loc",OBMisc.copyPoint(partLabel.position()));
            OBControl smallLine = lineControl.copy();
            smallLine.setWidth(lineLength);
            smallLine.setPosition(partLabel.position());
            smallLine.setBottom(partLabel.bottom());
            smallLine.show();
            right = smallLine.right();
            attachControl(smallLine);
            partLabel.setProperty("line",smallLine);
        }


        float repos = right - bounds().width();
        if(repos > 0)
        {
            for (int i = 0; i < lettersSelection.size(); i++)
            {
                OBLabel partLabel = lettersSelection.get(i);
                OBControl smallLine = (OBControl) partLabel.propertyValue("line");
                smallLine.setRight(smallLine.right() - repos);
                partLabel.setRight(partLabel.right() - repos);
            }
        }
        bigWordLabel.setColour(wordColour);
    }


    public void setCurrentScore(int count)
    {
        letterBoxCounter.setString ( String.format("%d",count));
        currentScore = count;
    }

    public void wordCompleted()
    {
        setStatus(STATUS_BUSY);
        gotItRight();
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                lockScreen();
                for(OBControl letter : lettersSelection)
                {
                    OBControl line = (OBControl)letter.propertyValue("line");
                    line.hide();
                }

                unlockScreen();
                List<OBAnim> anims = new ArrayList<>();
                for(OBControl part : lettersSelection)
                {
                    anims.add(OBAnim.moveAnim((PointF)part.settings.get("word_loc") ,part));

                }
                OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
                lockScreen();
                bigWordLabel.show();
                for(OBControl letter : lettersSelection)
                {
                    letter.hide();

                }

                unlockScreen();
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)bigWordLabel.propertyValue("dest_loc"),bigWordLabel),
                        OBAnim.scaleAnim((float)bigWordLabel.propertyValue("dest_scale"),bigWordLabel)),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
                waitForSecs(0.3f);
                OBPhoneme pho = (OBPhoneme)bigWordLabel.propertyValue("phoneme");
                pho.playAudio(controller,true);
                letterBox.openLid(null);
                letterBox.animateGlowsShow();
                letterBox.flyObjects(Arrays.asList((OBControl)bigWordLabel), false, true, null);
                setCurrentScore(currentScore+1);
                letterBox.animateGlowsHide();
                letterBox.closeLid(null);
                screenScrollSpeed += SCROLL_ACCELERATION;
                nest.show();
                nest.setLeft(bounds().width());

                startScrollLoop();
            }
        }) ;
    }

    public void gameLost()
    {
        setStatus(STATUS_BUSY);
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                playSFX("fail");
                PointF lastPoint = OB_Maths.locationForRect(0.1f, 1.1f, bounds());
                Path path = new Path();
                path.moveTo(bord.position().x, bord.position().y);
                PointF cp1 = new PointF(bord.position().x, bord.position().y - applyGraphicScale(500));
                PointF cp2 = new PointF(lastPoint.x, lastPoint.y - applyGraphicScale(300));
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, lastPoint.x, lastPoint.y);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(bord, path, false, 0), OBAnim.rotationAnim(-(float) Math.toRadians(720), bord)),
                        0.65, true, OBAnim.ANIM_EASE_IN, controller);
                waitForSecs(0.5f);
                bord.setRotation(0);
                setCurrentScore(0);
                screenScrollSpeed = INITIAL_SCROLL_SPEED;
                nest.show();
                nest.setLeft(bounds().width() + ((PointF) nest.propertyValue("dest_loc")).x * 1.2f);
                currentWordIndex = -1;
                startScrollLoop();
            }
        });

    }

}
