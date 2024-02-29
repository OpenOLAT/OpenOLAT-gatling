/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.util.Objects.requireNonNull;

/**
 * 
 * Initial date: 29 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IDEPathHelper {

  static final Path mavenSourcesDirectory;
  static final Path mavenResourcesDirectory;
  static final Path mavenBinariesDirectory;
  static final Path resultsDirectory;
  static final Path recorderConfigFile;

  static {
    try {
      Path projectRootDir = Paths.get(requireNonNull(IDEPathHelper.class.getResource("gatling.conf"), "Couldn't locate gatling.conf").toURI()).getParent().getParent().getParent();
      Path mavenTargetDirectory = projectRootDir.resolve("target");
      Path mavenSrcTestDirectory = projectRootDir.resolve("src").resolve("test");

      mavenSourcesDirectory = mavenSrcTestDirectory.resolve("java");
      mavenResourcesDirectory = mavenSrcTestDirectory.resolve("resources");
      mavenBinariesDirectory = mavenTargetDirectory.resolve("test-classes");
      resultsDirectory = mavenTargetDirectory.resolve("gatling");
      recorderConfigFile = mavenResourcesDirectory.resolve("recorder.conf");
    } catch (URISyntaxException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
}
