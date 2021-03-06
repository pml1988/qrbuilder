package com.skrymer.qrbuilder;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.skrymer.qrbuilder.exception.InvalidSizeException;
import com.skrymer.qrbuilder.exception.UnreadableDataException;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.skrymer.qrbuilder.decorator.ColoredQRCode.colorizeQRCode;
import static com.skrymer.qrbuilder.decorator.ImageOverlay.addImageOverlay;
import static org.testng.Assert.assertEquals;

/**
 * Tests for class ZXingQRCodeBuilder
 */
@Test
public class ZXingQRCodeBuilderTest {
  private static final int EXPECTED_QRCODE_WIDTH  = 250;
  private static final int EXPECTED_QRCODE_HEIGHT = 250;
  
  private QRCBuilder<BufferedImage> zXingQRCodeBuilder;
  
  @BeforeMethod
  public void setUp(){
    zXingQRCodeBuilder = new ZXingQRCodeBuilder();
  }
  
  @Test(invocationCount=10)
  public void testBuildQRCode_noOverlay_success() throws Exception {
    String expectedData = RandomStringUtils.randomAlphabetic(500);

    BufferedImage qrcode = zXingQRCodeBuilder.newQRCode()
                              .withSize(EXPECTED_QRCODE_WIDTH, EXPECTED_QRCODE_HEIGHT)
                                .and()
                              .withData(expectedData)
                              .toImage();
    
    assertQRCode(expectedData, qrcode);
  }

  @Test(invocationCount=10)
  public void testBuildQRCode_withImageOverlay_success() throws Exception {
    String expectedData = RandomStringUtils.randomAlphabetic(500);

    BufferedImage qrcode = zXingQRCodeBuilder.newQRCode()
                              .withSize(EXPECTED_QRCODE_WIDTH, EXPECTED_QRCODE_HEIGHT)
                                .and()
                              .withData(expectedData)
                                .and()
                              .decorate(addImageOverlay(getOverlay(), 1.0f, 0.25f))
                              .toImage();

    assertQRCode(expectedData, qrcode);
  }

  @Test(invocationCount=10)
  public void testBuildQRCode_createRedQRCode_success() throws Exception {
    String expectedData = RandomStringUtils.randomAlphabetic(500);

    BufferedImage qrcode = zXingQRCodeBuilder.newQRCode()
                              .withSize(EXPECTED_QRCODE_WIDTH, EXPECTED_QRCODE_HEIGHT)
                                .and()
                              .withData(expectedData)
                                .and()
                              .decorate(colorizeQRCode(Color.RED))
                              .toImage();

    assertQRCode(expectedData, qrcode);
  }

  @Test(expectedExceptions=InvalidSizeException.class)
  public void testBuildQRCode_widthIsZero_throwCouldNotCreateQRCodeException(){
    zXingQRCodeBuilder.newQRCode()
       .withSize(0, 1)
         .and()
       .withData("Some data")
       .toImage();
  }
	
  @Test(expectedExceptions=InvalidSizeException.class)
  public void testBuildQRCode_heightIsZero_throwCouldNotCreateQRCodeException(){
    zXingQRCodeBuilder.newQRCode()
       .withSize(1, 0)
         .and()
       .withData("Some data")
       .toImage();
  }
	
  @Test(expectedExceptions=UnreadableDataException.class) 
  public void testBuildQRCode_overlayToBig_throwUnreadableDataException() throws Exception {
    zXingQRCodeBuilder.newQRCode()
       .withSize(250, 250)
         .and()
       .withData("Some data")
         .and()
       .decorate(addImageOverlay(getOverlay(), 1.0f, 0.35f))
       .toImage();
  }
		
//----------------------
// Helper methods
//----------------------
	
  private void assertQRCode(String expectedData, BufferedImage qrcode) throws Exception {
    assertEquals(EXPECTED_QRCODE_WIDTH,  qrcode.getWidth());
    assertEquals(EXPECTED_QRCODE_HEIGHT, qrcode.getHeight());
    assertEquals(expectedData,  decodeQRCode(qrcode));
  }
	
  private String decodeQRCode(BufferedImage qrcode) throws Exception{
    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(qrcode)));
    Map<DecodeHintType, Object> decodeHints = new HashMap<DecodeHintType, Object>();
    decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

    Result result = new QRCodeReader().decode(binaryBitmap, decodeHints);
    
    return result.getText();
  }
	
  private BufferedImage getOverlay() throws Exception {
    return ImageIO.read(new File("src/test/resources/images/skull_bw.png"));
  }
}
