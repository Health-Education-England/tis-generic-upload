package com.transformuk.hee.tis.genericupload.service.util;

import static org.hamcrest.Matchers.contains;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MultiValueUtilTest {

  @Test
  public void shouldGetEmptyListWhenValueIsNull() {
    List<String> list = MultiValueUtil.splitMultiValueField(null);
    Assert.assertTrue(list.isEmpty());
  }

  @Test
  public void shouldGetEmptyListWhenValueIsEmpty() {
    String value = "";
    List<String> list = MultiValueUtil.splitMultiValueField(value);
    Assert.assertTrue(list.isEmpty());
  }

  @Test
  public void shouldGetListWhenSingleValue() {
    String value = "value1";
    List<String> list = MultiValueUtil.splitMultiValueField(value);
    Assert.assertEquals(list.size(), 1);
    Assert.assertEquals(list.get(0), value);
  }

  @Test
  public void shouldGetListWhenMultiValue() {
    String value = "value1;value2";
    List<String> list = MultiValueUtil.splitMultiValueField(value);
    Assert.assertEquals(list.size(), 2);
    Assert.assertThat(list, contains("value1", "value2"));
  }

  @Test
  public void shouldTrimValues() {
    String value = " value1 ; value2 ";
    List<String> list = MultiValueUtil.splitMultiValueField(value);
    Assert.assertEquals(list.size(), 2);
    Assert.assertThat(list, contains("value1", "value2"));
  }
}
