package www.tupobi.top.between.bean;

public class AppVersion {
    private String versionName;
    private String appName;
    private boolean isLatest;
    private String date;
    private String downloadUrl;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "versionName='" + versionName + '\'' +
                ", appName='" + appName + '\'' +
                ", isLatest=" + isLatest +
                ", date='" + date + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
