package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.IosApp;
import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.mobile.utils.ZipUtil;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IosSimulatorInstallAppTask extends MobilePluginTask {

    public IosSimulatorInstallAppTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Installs an app onto a specific iOS simulator");
    }

    public void doTaskAction() throws Exception {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
                getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
                getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
                getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        File ipa = findIpa(getProject().getProperties().get("uitest.artifact.classifier"));
        File app = extractApp(ipa);
        IosApp iosApp = getAppInfo(app);
        logger.info("Installing iOS app { bundle:{}, version:{} } on {}",
                iosApp.getBundleIdentifier(), iosApp.getBundleVersion(), iosSimulator);
        if (IosSimulatorUtil.installApp(iosSimulator, app).isSuccess()) {
            if (System.getenv("TEAMCITY_VERSION") != null) {
                logger.lifecycle("##teamcity[setParameter name='env.UITEST_BUNDLE_ID' value='{}']", iosApp.getBundleIdentifier());
                logger.lifecycle("##teamcity[buildNumber '{}']", iosApp.getBundleVersion());
            }
        } else {
            stopWithError("Failed to install app on iosSimulator");
        }
    }

    private File findIpa(Object classifier) throws IOException {
        List<File> ipas = CommonUtil.findIosIpasInDirectory(getProject().getProjectDir());
        if (ipas.size() == 1) {
            logger.info("Found only a single ipa '{}'", ipas.get(0).getName());
            return ipas.get(0);
        } else if (ipas.isEmpty()) {
            throw new IOException("No ipa files found");
        } else if (classifier == null) {
            throw new IOException("Multiple ipa files found and classifier was not provided");
        } else {
            for (File ipa : ipas) {
                if (ipa.getName().toLowerCase().endsWith(classifier.toString().toLowerCase()+".ipa")) {
                    logger.info("Found ipa matching provided classifier '{}'", ipa.getName());
                    return ipa;
                }
            }
            throw new IOException("Multiple ipa files found and classifier did not match");
        }
    }

    private File extractApp(File ipa) throws IOException {
        File payloadDir = new File("build/gmp-temp/"+ipa.getName());
        ZipUtil.extractAll(ipa, payloadDir);
        return CommonUtil.findIosAppsInDirectory(payloadDir).get(0);
    }

    private IosApp getAppInfo(File app) throws IOException {
        return new IosApp(app);
    }

}
