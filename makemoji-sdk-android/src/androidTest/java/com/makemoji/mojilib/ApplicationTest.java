package com.makemoji.mojilib;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import com.makemoji.mojilib.model.MojiModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * To run test, right click file in project view on the left and click Run ApplicationTest
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {

    Application application ;
    public ApplicationTest() {
        super(Application.class);
    }
    @Override
    protected void setUp()throws Exception
    {
        super.setUp();
        createApplication();
        application  = getApplication();
        Moji.initialize(getApplication(),"78f8c331e715ae73cf9dc0997e3ca7276ad5b35e");
    }
    @Before
    public void TestSimpleCreate() {
        Context context = InstrumentationRegistry.getContext();
        setContext(context);
        createApplication();
        application = getApplication();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Moji.initialize(getApplication(),"78f8c331e715ae73cf9dc0997e3ca7276ad5b35e");
        assertNotNull(application);
    }

    @Test
    public void plainTestAndBack() throws Exception {
        String html = "Test <img src=\"http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" id=\"1034\" name=\"alien\" link=\"\">";
        String plainText = Moji.htmlToPlainText(html);
        assertEquals(plainText,"Test [alien.Gg]");

    }
    @Test
    public void htmlPlainConsistency(){
        String plain = "[Avocado.2At][Cherry.5g] cool [Bowl of Cream.da]";
        Spanned plainSpan = Moji.plainTextToSpanned(plain);
        String html = Moji.plainTextToHtml(plain);
        ParsedAttributes spaceParse = Moji.parseHtml(html,null,true,true);
        Spanned htmlSpan = spaceParse.spanned;
        assertTrue(SSBEquals((SpannableStringBuilder)htmlSpan,plainSpan));
        String toPlain = Moji.htmlToPlainText(html);
        assertEquals(toPlain,plain);
        String htmlFromSpan = Moji.toHtml(htmlSpan);
        assertEquals(htmlFromSpan,html);
        ParsedAttributes noSpaceParse = Moji.parseHtml(html,null,true,false);
        String noSpaceHtml = Moji.toHtml(noSpaceParse.spanned);
        assertEquals(htmlFromSpan,noSpaceHtml);//html consistent no matter editText padding.

    }

    @Test
    public void htmlAndBack(){
        String originalHtml = "<p dir=\"auto\" style=\"margin-bottom:16px;font-family:'.Helvetica Neue Interface';font-size:16px;\">" +
                "<span style=\"color:#000000;\">abc<img style=\"vertical-align:text-bottom;width:20px;height:20px;\" " +
                "id=\"12\"src=\"http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" name=\"moji\" link=\"\">123" +
                "<img style=\"vertical-align:text-bottom;width:20px;height:20px;\" id=\"523\"src=\"" +
                "http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" name=\"roarke\" link=\"www.google.com\"></p>";
        Spanned spanned = Moji.parseHtml(originalHtml,null,true).spanned;
        String newHtml = Moji.toHtml(spanned);
        assertEquals(originalHtml,newHtml);

    }
    @Test
    public void offlineImage(){
        Uri webUri = Uri.parse("www.google.com");
        Uri offlineUri = Uri.parse("file:///android_asset/makemoji/sdkimages/"+"www.google.com");
        Uri mojiWebUri = Moji.uriImage("www.google.com");
        Moji.setEnableUpdates(false);
        Uri mojiOfflineUri = Moji.uriImage("www.google.com");

        assertEquals(webUri,mojiWebUri);
        assertEquals(mojiOfflineUri,offlineUri);
    }
    @Test
    public void testRescentList(){
        MojiModel model = new MojiModel("testing","www.google.com");
        model.id=12;
        MojiModel second = new MojiModel("second","www.google.com");
        second.id=4;
        RecentPopulator.clearRecents();
        assert RecentPopulator.getRecents().isEmpty();
        RecentPopulator.addRecent(model);
        RecentPopulator.addRecent(second);
        assert RecentPopulator.getRecents().get(0).name.equals("second");
        RecentPopulator.addRecent(model);
        assert RecentPopulator.getRecents().get(0).name.equals("model");
        assert RecentPopulator.getRecents().size()==2;
        RecentPopulator.clearRecents();
        assert RecentPopulator.getRecents().isEmpty();
    }

    public boolean SSBEquals(SpannableStringBuilder ssb, Object o){
        String s1 = ssb.toString().replace("\u0000", "");
        String s2 = o.toString().replace("\u0000", "");
        if (o instanceof Spanned &&
               s1.contentEquals(s2)) {
            Spanned other = (Spanned) o;
            // Check span data
            Object[] otherSpans = other.getSpans(0, other.length(), Object.class);
            Object[] mSpans = ssb.getSpans(0, other.length(), Object.class);
            if (mSpans.length == otherSpans.length) {
                for (int i = 0; i < mSpans.length; ++i) {
                    Object thisSpan = mSpans[i];
                    Object otherSpan = otherSpans[i];
                    if (thisSpan == this) {
                        if (other != otherSpan ||
                                ssb.getSpanStart(thisSpan) != other.getSpanStart(otherSpan) ||
                                ssb.getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) ||
                                ssb.getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                            return false;
                        }
                    } else if (
                            ssb.getSpanStart(thisSpan) != other.getSpanStart(otherSpan) ||
                            ssb.getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) ||
                            ssb.getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)
                            || !(otherSpan instanceof MojiSpan && thisSpan instanceof MojiSpan &&
                                    ((MojiSpan) otherSpan).equivelant(thisSpan))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}