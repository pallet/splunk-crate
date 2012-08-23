(ns pallet.crate.splunk
  "Install and configure splunk"
  (:require
   [pallet.action :as action]
   [pallet.action.package :as package]
   [pallet.action.remote-file :as remote-file]
   [pallet.argument :as argument]
   [pallet.compute :as compute]
   [pallet.session :as session]
   [pallet.stevedore :as stevedore])
  (:use
    [pallet.script.lib :only [tmp-dir]]))

(def build
  {"4.3.3" 128297
   "4.3.2" 123586
   "4.3.1" 119532
   "4.3" 115073
   "4.2.5" 113966
   "4.2" 96430
   "4.1.8" 97242
   "4.1.4" 82143
   "4.1.3" 80534
   "4.1.2" 79191
   "4.1.1" 78281
   "4.1" 77833
   "4.0.11" 79031
   "4.0.10" 77146
   "4.0.9" 74233
   "4.0.8" 73243
   "4.0.7" 72459
   "4.0.6" 70313
   "4.0.5" 69401
   "4.0.4" 67724
   "4.0.3" 65638
   "4.0.2" 64889
   "4.0.1" 64658
   "3.4.14" 79166
   "3.4.13" 75215
   "3.4.12" 69236
   "3.4.11" 65313
   "3.4.10" 60883
   "3.4.9" 57762
   "3.4.8" 54309
   "3.4.6" 51113
   "3.4.5" 47883
   "3.4.3" 46779
   "3.4.2" 46047
   "3.4.1" 45588
   "3.4" 44873
   "3.3.4" 43000
   "3.3.3" 42717
   "3.3.2" 41320
   "3.3.1" 39933
   "3.3" 38914
   "3.2.6" 38259
   "3.2.5" 38160
   "3.2.4" 37025
   "3.2.3" 35555
   "3.2.2" 34603
   "3.2.1" 34291
   "3.2" 33572
   "3.1.5" 31521
   "3.1.4" 30364
   "3.1.3" 28524
   "3.1.2" 28096
   "3.1.1" 27147
   "3.1" 26228
   "3.0.2" 24828
   "3.0.1" 24078
   "3.0" 23043
   "2.2.6" 21120
   "2.2.3" 18173
   "2.2.1" 17100
   "2.2" 15292
   "2.1.3" 14652
   "2.1.2" 14524})

(defn appnamesuffix [forwarder]
  (if forwarder "forwarder" ""))

(defn debfile [session version]
  (if (compute/is-64bit? (session/target-node session))
    (format "splunk-%s-%d-linux-2.6-amd64.deb" version (build version))
    (format "splunk-%s-%d-linux-2.6-intel.deb" version (build version))))

(defn rpmfile [session version forwarder]
  (if (compute/is-64bit? (session/target-node session))
    (format "splunk%s-%s-%d-linux-2.6-x86_64.rpm" (appnamesuffix forwarder) version (build version))
    (format "splunk%s-%s-%d-i386.rpm" (appnamesuffix forwarder) version (build version))))

(defn url-segment [forwarder]
  (if forwarder "universalforwarder" "splunk"))

(defn url [version file forwarder]
  (format
   "http://www.splunk.com/page/download_track?file=%s/%s/linux/%s&ac=&wget=true&name=wget&typed=release"
   version (url-segment forwarder) file))

(defn md5-url
  "Unfortunately the md5 file returned is not usable with md5sum --check"
  [version file forwarder]
  (format
   "http://www.splunk.com/page/download_track?file=%s/%s/linux/%s.md5&ac=&wget=true&name=wget&typed=release"
   version (url-segment forwarder) file))

(defn get-splunk-path [forwarder]
  (format "/opt/splunk%s/bin/splunk" (appnamesuffix forwarder)))

(def remote-file* (action/action-fn remote-file/remote-file-action))
(action/def-bash-action install
  [session & {:keys [version forwarder]}]
  (case (session/packager session)
    :aptitude
    (let [f (debfile session version)
          deb (str (stevedore/script (~tmp-dir)) "/" f)]
      (stevedore/checked-commands
       "Install splunk"
       (remote-file* session deb :url (url version f forwarder))
       (stevedore/script
        (if-not (file-exists? ~(get-splunk-path forwarder))
          (do
            (dpkg -i (quoted ~deb))
            (~(get-splunk-path forwarder) start "--accept-license")
            (~(get-splunk-path forwarder) enable boot-start))))))
    :yum
    (let [f (rpmfile session version forwarder)
          rpm (str (stevedore/script (~tmp-dir)) "/" f)]
      (stevedore/checked-commands
       "Install splunk"
       (remote-file* session rpm :url (url version f forwarder))
       (stevedore/script
        (if-not (file-exists? ~(get-splunk-path forwarder))
          (do
            (rpm -i (quoted ~rpm))
            (~(get-splunk-path forwarder) start "--accept-license")
            (~(get-splunk-path forwarder) enable boot-start))))))))

(defn splunk
  [session & {:keys [version forwarder] :or {version "4.3.3" forwarder false}}]
  (install session :version version :forwarder forwarder))

(defn format-section
  [m]
  (reduce #(str %1 (format "%s = %s\n" (name (first %2)) (second %2))) "" m))

(defn format-conf
  [maps]
  (reduce
   #(str %1 (format
             "[%s]\n%s\n"
             (name (first %2)) (format-section (second %2))))
   ""
   maps))

(defn fifo-input
  [path & {:keys [host index sourcetype source queue] :as options}]
  {(format "fifo://%s" path) options})

(defn configure
  [session & {:keys [inputs host] :or {inputs {}}}]
  (remote-file/remote-file
   session
   "/opt/splunk/etc/system/local/inputs.conf"
   :content (format-conf
             (merge
              inputs
              {:default {:host (or host
                                   (compute/hostname
                                    (session/target-node session)))}}))
   :owner "splunk" :group "splunk"))
