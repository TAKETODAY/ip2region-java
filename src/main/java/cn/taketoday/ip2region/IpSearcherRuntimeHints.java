package cn.taketoday.ip2region;

import org.jspecify.annotations.Nullable;

import infra.aot.hint.RuntimeHints;
import infra.aot.hint.RuntimeHintsRegistrar;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2026/4/5 22:31
 */
class IpSearcherRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    hints.resources().registerPattern("classpath:ip2region.xdb");
  }

}
