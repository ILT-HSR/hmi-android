Search.setIndex({docnames:["implementation/GCS","implementation/MainModel","implementation/ResourceModel","implementation/driver","implementation/driver/AerialVehicle","implementation/driver/Command","implementation/driver/Input","implementation/driver/NullCommand","implementation/driver/NullPlatform","implementation/driver/Payload","implementation/driver/Platform","implementation/driver/PlatformModel","implementation/driver/SerialPlatform","implementation/driver/Vehicle","implementation/driver/access/InputManager","implementation/driver/access/PayloadProvider","implementation/driver/access/PlatformManager","implementation/driver/channel/ChannelFactory","implementation/driver/channel/SerialDataChannel","implementation/driver/channel/SerialDataChannelFactory","implementation/driver/input/HandheldControls","implementation/driver/mavlink/MAVLinkCommand","implementation/driver/mavlink/MAVLinkPayload","implementation/driver/mavlink/MAVLinkPlatform","implementation/driver/mavlink/payload/Gripper","implementation/driver/mavlink/payload/NullPayload","implementation/driver/mavlink/platform/BasicPlatform","implementation/driver/mavlink/platform/CommonPlatform","implementation/driver/mavlink/platform/PixhawkPX4","implementation/driver/mavlink/support/LongCommand","implementation/driver/mavlink/support/MAVLinkExecution","implementation/driver/mavlink/support/MAVLinkMissionCommand","implementation/driver/mavlink/support/MessageID","implementation/driver/mavlink/support/Messages","implementation/driver/mavlink/support/NavigationFrame","implementation/driver/mavlink/support/VehicleMode","implementation/index","implementation/mission","implementation/mission/Execution","implementation/mission/Mission","implementation/mission/Need","implementation/mission/Result","implementation/mission/Scheduler","implementation/mission/access/NeedManager","implementation/mission/need/CallIn","implementation/mission/need/RadiationMap","implementation/mission/need/parameter/Altitude","implementation/mission/need/parameter/Cargo","implementation/mission/need/parameter/Parameter","implementation/mission/need/parameter/Region","implementation/mission/need/parameter/SpeedLimit","implementation/mission/need/parameter/Target","implementation/mission/need/task/LimitTravelSpeed","implementation/mission/need/task/MoveToPosition","implementation/mission/need/task/ReturnToHome","implementation/mission/need/task/RunPlan","implementation/mission/need/task/TakeOff","implementation/mission/need/task/Task","implementation/mission/need/task/TriggerPayload","implementation/resource","implementation/resource/Capability","implementation/resource/CapabilityDescriptor","implementation/resource/LocalResource","implementation/resource/Resource","implementation/resource/ResourceManager","implementation/support","implementation/support/concurrent/Concurrent","implementation/support/file/Plan","implementation/support/geo/GPSPosition","implementation/support/geo/WGS89Position","implementation/support/usb/DeviceHandler","implementation/ui","implementation/ui/MainActivity","implementation/ui/MenuFragmentID","implementation/ui/SettingsActivity","implementation/ui/mission/MissionResultsFragment","implementation/ui/mission/MissionResultsRecyclerViewAdapter","implementation/ui/mission/MissionStatusesFragment","implementation/ui/mission/MissionStatusesRecyclerViewAdapter","implementation/ui/mission/ResultItem","implementation/ui/mission/need/NeedInstructionFragment","implementation/ui/mission/need/NeedInstructionRecyclerViewAdapter","implementation/ui/mission/need/NeedItem","implementation/ui/mission/need/NeedItemFactory","implementation/ui/mission/need/NeedsFragment","implementation/ui/mission/need/NeedsRecyclerViewAdapter","implementation/ui/mission/need/parameter/ParameterItem","implementation/ui/mission/need/parameter/ParameterItemFactory","implementation/ui/mission/need/parameter/configurator/AltitudeConfigurator","implementation/ui/mission/need/parameter/configurator/CargoConfigurator","implementation/ui/mission/need/parameter/configurator/RegionConfigurator","implementation/ui/mission/need/parameter/configurator/SpeedLimitConfigurator","implementation/ui/mission/need/parameter/configurator/TargetConfigurator","implementation/ui/settings/SettingsFragment","index"],envversion:{"sphinx.domains.c":1,"sphinx.domains.changeset":1,"sphinx.domains.cpp":1,"sphinx.domains.javascript":1,"sphinx.domains.math":2,"sphinx.domains.python":1,"sphinx.domains.rst":1,"sphinx.domains.std":1,sphinx:56},filenames:["implementation/GCS.rst","implementation/MainModel.rst","implementation/ResourceModel.rst","implementation/driver.rst","implementation/driver/AerialVehicle.rst","implementation/driver/Command.rst","implementation/driver/Input.rst","implementation/driver/NullCommand.rst","implementation/driver/NullPlatform.rst","implementation/driver/Payload.rst","implementation/driver/Platform.rst","implementation/driver/PlatformModel.rst","implementation/driver/SerialPlatform.rst","implementation/driver/Vehicle.rst","implementation/driver/access/InputManager.rst","implementation/driver/access/PayloadProvider.rst","implementation/driver/access/PlatformManager.rst","implementation/driver/channel/ChannelFactory.rst","implementation/driver/channel/SerialDataChannel.rst","implementation/driver/channel/SerialDataChannelFactory.rst","implementation/driver/input/HandheldControls.rst","implementation/driver/mavlink/MAVLinkCommand.rst","implementation/driver/mavlink/MAVLinkPayload.rst","implementation/driver/mavlink/MAVLinkPlatform.rst","implementation/driver/mavlink/payload/Gripper.rst","implementation/driver/mavlink/payload/NullPayload.rst","implementation/driver/mavlink/platform/BasicPlatform.rst","implementation/driver/mavlink/platform/CommonPlatform.rst","implementation/driver/mavlink/platform/PixhawkPX4.rst","implementation/driver/mavlink/support/LongCommand.rst","implementation/driver/mavlink/support/MAVLinkExecution.rst","implementation/driver/mavlink/support/MAVLinkMissionCommand.rst","implementation/driver/mavlink/support/MessageID.rst","implementation/driver/mavlink/support/Messages.rst","implementation/driver/mavlink/support/NavigationFrame.rst","implementation/driver/mavlink/support/VehicleMode.rst","implementation/index.rst","implementation/mission.rst","implementation/mission/Execution.rst","implementation/mission/Mission.rst","implementation/mission/Need.rst","implementation/mission/Result.rst","implementation/mission/Scheduler.rst","implementation/mission/access/NeedManager.rst","implementation/mission/need/CallIn.rst","implementation/mission/need/RadiationMap.rst","implementation/mission/need/parameter/Altitude.rst","implementation/mission/need/parameter/Cargo.rst","implementation/mission/need/parameter/Parameter.rst","implementation/mission/need/parameter/Region.rst","implementation/mission/need/parameter/SpeedLimit.rst","implementation/mission/need/parameter/Target.rst","implementation/mission/need/task/LimitTravelSpeed.rst","implementation/mission/need/task/MoveToPosition.rst","implementation/mission/need/task/ReturnToHome.rst","implementation/mission/need/task/RunPlan.rst","implementation/mission/need/task/TakeOff.rst","implementation/mission/need/task/Task.rst","implementation/mission/need/task/TriggerPayload.rst","implementation/resource.rst","implementation/resource/Capability.rst","implementation/resource/CapabilityDescriptor.rst","implementation/resource/LocalResource.rst","implementation/resource/Resource.rst","implementation/resource/ResourceManager.rst","implementation/support.rst","implementation/support/concurrent/Concurrent.rst","implementation/support/file/Plan.rst","implementation/support/geo/GPSPosition.rst","implementation/support/geo/WGS89Position.rst","implementation/support/usb/DeviceHandler.rst","implementation/ui.rst","implementation/ui/MainActivity.rst","implementation/ui/MenuFragmentID.rst","implementation/ui/SettingsActivity.rst","implementation/ui/mission/MissionResultsFragment.rst","implementation/ui/mission/MissionResultsRecyclerViewAdapter.rst","implementation/ui/mission/MissionStatusesFragment.rst","implementation/ui/mission/MissionStatusesRecyclerViewAdapter.rst","implementation/ui/mission/ResultItem.rst","implementation/ui/mission/need/NeedInstructionFragment.rst","implementation/ui/mission/need/NeedInstructionRecyclerViewAdapter.rst","implementation/ui/mission/need/NeedItem.rst","implementation/ui/mission/need/NeedItemFactory.rst","implementation/ui/mission/need/NeedsFragment.rst","implementation/ui/mission/need/NeedsRecyclerViewAdapter.rst","implementation/ui/mission/need/parameter/ParameterItem.rst","implementation/ui/mission/need/parameter/ParameterItemFactory.rst","implementation/ui/mission/need/parameter/configurator/AltitudeConfigurator.rst","implementation/ui/mission/need/parameter/configurator/CargoConfigurator.rst","implementation/ui/mission/need/parameter/configurator/RegionConfigurator.rst","implementation/ui/mission/need/parameter/configurator/SpeedLimitConfigurator.rst","implementation/ui/mission/need/parameter/configurator/TargetConfigurator.rst","implementation/ui/settings/SettingsFragment.rst","index.rst"],objects:{"":{"arm()":[23,2,1,""],"changeAltitude(altitude:)":[4,2,1,""],"class Altitude":[46,3,1,""],"class CallIn":[44,3,1,""],"class Cargo":[47,3,1,""],"class CommonPlatform":[27,3,1,""],"class MainModel":[1,3,1,""],"class MainModelEvent":[1,3,1,""],"class Mission":[39,3,1,""],"class MissionResultsFragment":[75,3,1,""],"class MissionStatusesFragment":[77,3,1,""],"class NeedsFragment":[84,3,1,""],"class RadiationMap":[45,3,1,""],"class Region":[49,3,1,""],"class SerialDataChannel":[18,3,1,""],"class SpeedLimit":[50,3,1,""],"class Target":[51,3,1,""],"constructor(descriptor:value:)":[60,0,1,""],"constructor(device:)":[1,0,1,""],"constructor(fPort:)":[18,0,1,""],"constructor(id:component:)":[33,0,1,""],"constructor(id:type:)":[61,0,1,""],"constructor(latitude:longitude:altitude:)":[69,0,1,""],"constructor(mission:)":[1,0,1,""],"constructor(need:)":[39,0,1,""],"constructor(position:)":[69,0,1,""],"constructor(result:)":[1,0,1,""],"create(context:port:configuration:)":[18,4,1,""],"createArmMessage(sender:target:schema:)":[33,2,1,""],"createHeartbeatMessage(sender:schema:)":[33,2,1,""],"createRequestAutopilotCapabilitiesMessage(sender:target:schema:)":[33,2,1,""],"data_class Altitude":[4,5,1,""],"data_class Capability":[60,5,1,""],"data_class CapabilityDescriptor":[61,5,1,""],"data_class GPSPosition":[68,5,1,""],"data_class InputDeviceAvailable":[1,5,1,""],"data_class MAVLinkSystem":[33,5,1,""],"data_class MissionAvailable":[1,5,1,""],"data_class NeedAvailable":[1,5,1,""],"data_class NeedConfigurationStarted":[1,5,1,""],"data_class NeedUnavailable":[1,5,1,""],"data_class ResultAvailable":[1,5,1,""],"data_class WGS89Position":[69,5,1,""],"disarm()":[23,2,1,""],"enum_class MessageID":[32,6,1,""],"enum_class Status":[63,6,1,""],"enum_class VehicleMode":[35,6,1,""],"executeOn(resource:)":[57,2,1,""],"has(capability:)":[63,2,1,""],"interface AerialVehicle":[4,7,1,""],"interface MAVLinkPlatform":[23,7,1,""],"interface Need":[40,7,1,""],"interface Parameter":[48,7,1,""],"interface Platform":[10,7,1,""],"interface Resource":[63,7,1,""],"interface SerialPlatform":[12,7,1,""],"interface Task":[57,7,1,""],"interface Vehicle":[13,7,1,""],"land()":[4,2,1,""],"limitTravelSpeed(speed:)":[13,2,1,""],"markAs(status:)":[63,2,1,""],"moveTo(position:)":[13,2,1,""],"object InputDeviceUnavailable":[1,8,1,""],"object MissionOverviewRequested":[1,8,1,""],"object NeedConfigurationAborted":[1,8,1,""],"object NeedConfigurationFinished":[1,8,1,""],"object NeedOverviewRequested":[1,8,1,""],"resultToString()":[48,2,1,""],"returnToHome()":[13,2,1,""],"returnToLaunch()":[4,2,1,""],"submit(event:)":[1,2,1,""],"takeOff(altitude:)":[4,2,1,""],activeInputDevice:[1,1,1,""],activeMenuFragment:[1,1,1,""],activeMissions:[1,1,1,""],activeNeed:[1,1,1,""],availableNeeds:[1,1,1,""],capabilities:[63,1,1,""],currentPosition:[10,1,1,""],driverId:[63,1,1,""],execution:[10,1,1,""],id:[63,1,1,""],isAlive:[10,1,1,""],isAvailable:[63,1,1,""],missionResults:[1,1,1,""],name:[10,1,1,""],parameterList:[40,1,1,""],payload:[10,9,1,""],payloadDriverId:[63,1,1,""],plaform:[63,9,1,""],requirements:[40,1,1,""],resource:[40,1,1,""],result:[48,9,1,""],schema:[23,1,1,""],senderSystem:[23,1,1,""],status:[63,1,1,""],targetSystem:[23,1,1,""],tasks:[40,1,1,""]},Altitude:{"constructor(meters:)":[4,0,1,""]}},objnames:{"0":["kotlin","constructor","Kotlin constructor"],"1":["kotlin","val","Kotlin constant"],"2":["kotlin","fun","Kotlin fun"],"3":["kotlin","class","Kotlin class"],"4":["kotlin","static_fun","Kotlin static fun"],"5":["kotlin","data_class","Kotlin data class"],"6":["kotlin","enum_class","Kotlin enum class"],"7":["kotlin","interface","Kotlin interface"],"8":["kotlin","object","Kotlin object"],"9":["kotlin","var","Kotlin variable"]},objtypes:{"0":"kotlin:constructor","1":"kotlin:val","2":"kotlin:fun","3":"kotlin:class","4":"kotlin:static_fun","5":"kotlin:data_class","6":"kotlin:enum_class","7":"kotlin:interface","8":"kotlin:object","9":"kotlin:var"},terms:{"abstract":[39,57,63],"boolean":[10,63],"byte":18,"class":[1,4,18,27,32,33,35,39,44,45,46,47,49,50,51,60,61,63,68,69,75,77,84],"enum":[32,35,63],"float":68,"function":[4,13],"int":[33,46,69],"long":33,"new":[1,18,33],"null":[10,40],"return":[4,10,13,33,40,63],"static":18,"true":63,"var":[10,48,63],GCS:23,GPS:[13,68],IDs:32,IFS:[1,4,10,12,13,18,23,27,33,35,39,40,44,45,46,47,49,50,51,57,60,61,63,68,69],The:[1,4,10,13,18,23,33,40,48,57,63],about:63,access:[3,10,36,37],acquir:63,activ:1,activeinputdevic:1,activemenufrag:1,activemiss:1,activene:1,add:[75,77],addit:[75,77],addition:10,aerial:4,aerialvehicl:[3,23,36],all:[1,4,10,13],allow:[10,63],altitud:[4,36,37,50,68,69],altitudeconfigur:[36,71],ani:63,api:23,applic:[1,18],arm:[23,33],arriv:1,associ:[23,40],attach:[10,18],author:[1,4,10,12,13,18,23,27,33,35,39,40,44,45,46,47,49,50,51,57,60,61,63,68,69],auto_arm:35,auto_disarm:35,autopilot:33,autopilot_vers:32,avail:[1,63],availablene:1,availableresourc:60,base:[40,48,60],basic:[27,57],basicplatform:[3,36],becam:1,becom:1,been:[1,10],being:1,busi:63,button:[75,77],bytechannel:27,callin:[36,37],can:[10,35,60,63],capabilitydescriptor:[36,59,60],capabl:[33,36,40,57,59,61,63],cargo:[36,37,44],cargoconfigur:[36,71],certain:45,chang:[4,63],changealtitud:4,channel:[3,27,36],channelfactori:[3,36],check:[10,63],chosen:44,combin:[75,77],command:[3,4,10,13,23,33,36,39,57],command_ack:32,command_long:32,common:[4,13,27],commonli:32,commonplatform:[3,36],commun:[10,18],complet:[40,48],compon:33,concret:40,concurr:[36,65],configur:[1,18,36,46,47,48,50,51,71],connect:[1,12],constructor:[1,4,18,27,33,39,44,45,60,61,68,69],contain:33,content:[3,36,37,59,65,71,94],context:18,convert:[68,69],coordin:[68,69],coupl:10,creat:[18,33],createarmmessag:33,createheartbeatmessag:33,createrequestautopilotcapabilitiesmessag:33,current:[1,10],currentposit:10,data:[1,4,33,60,61,68,69],defin:[1,4,13,49,61],deriv:[1,27],describ:35,descriptor:[60,61],desir:[46,47,50,63],determin:10,devic:[1,18],devicehandl:[36,65],disarm:23,document:[3,36,37,59,65,71],doe:1,doubl:[4,13,50,68],driver:[36,39,63,94],driverid:[10,63],drone:10,drop:44,each:[10,63],encapsul:39,enumer:[32,35],etc:10,event:1,execut:[10,36,37,57],executeon:57,facilit:18,factiveinputdevic:1,factivemenufrag:1,factivemiss:1,factivene:1,fail:63,fals:63,favailablene:1,file:[36,65],filter:[60,63],finish:1,fmissionresult:1,fport:18,fragment:[1,75,77,84],from:[1,27],fulfil:40,fun:[1,4,13,18,23,33,39,48,57,60,61,63,68,69],further:63,gcs:27,gener:[10,23,36,45,94],geo:[36,65],get:10,given:[4,10,13,18,57,61,63],global_position_int:32,gpsposit:[10,13,36,49,51,65,69],gripper:[3,36],guided_arm:35,guided_disarm:35,handheldcontrol:[3,36],handl:[36,94],has:[1,10,49,61,63],have:12,heartbeat:[32,33],heat:45,hold:[68,69],hsr:27,identif:63,identifi:[1,12,23],iff:63,ifs:27,implement:[10,27,44,45,46,47,49,50,51,94],inform:63,infrastructur:[36,94],input:[1,3,18,36,75,77,84],inputdeviceavail:1,inputdeviceunavail:1,inputmanag:[3,36],instanc:40,institut:[1,4,10,12,13,18,23,27,33,35,39,40,44,45,46,47,49,50,51,57,60,61,63,68,69],instruct:[4,13],integ:69,interest:1,interfac:[4,10,12,13,18,23,36,40,48,57,63,94],isal:10,isavail:63,item:[75,77,84],its:[4,13],itself:10,land:4,latitud:[68,69],launch:[4,13],level:57,limittravelspe:[13,36,37],list:[1,40,49,57,63,75,77,84],listen:[75,77,84],livedata:1,localresourc:[36,59],locat:44,logic:1,longcommand:[3,36],longitud:[68,69],mai:10,main:[1,4,18,27,33,39,44,45,60,61,68,69],mainact:[36,71],mainmodelev:1,make:1,manag:[36,94],manual_arm:35,manual_disarm:35,map:45,marka:63,mavlink:[3,36],mavlinkcommand:[3,36],mavlinkexecut:[3,36],mavlinkmessag:33,mavlinkmissioncommand:[3,36],mavlinkpayload:[3,36],mavlinkplatform:[3,36],mavlinkschema:[23,33],mavlinksystem:[23,33],maximum:13,menu:1,menufragmentid:[1,36,71],messag:[3,23,32,36],messageid:[3,36],meter:4,minim:10,mission:[1,10,36,71,94],mission_ack:32,mission_count:32,mission_item:32,mission_request:32,missionavail:1,missionoverviewrequest:1,missionresult:1,missionresultsfrag:[36,71],missionresultsrecyclerviewadapt:[36,71],missionstatusesfrag:[36,71],missionstatusesrecyclerviewadapt:[36,71],mode:[35,45],model:1,move:13,moveto:13,movetoposit:[36,37],must:1,name:10,navigationfram:[3,36],necessari:40,need:[1,36,37,39,71,75,77],needavail:1,needconfigurationabort:1,needconfigurationfinish:1,needconfigurationstart:1,needinstructionfrag:[36,71],needinstructionrecyclerviewadapt:[36,71],needitem:[36,71],needitemfactori:[36,71],needmanag:[36,37],needoverviewrequest:1,needsfrag:[36,71],needsrecyclerviewadapt:[36,71],needunavail:1,nullcommand:[3,36],nullpayload:[3,36],nullplatform:[3,36],number:10,object:1,off:4,one:63,orient:18,otherwis:63,output:18,outsid:1,overrid:[44,45],overview:1,paramet:[18,33,36,37,40,57,63,71],parameteritem:[36,71],parameteritemfactori:[36,71],parameterlist:40,payload:[3,10,36,63],payloaddriverid:63,payloadprovid:[3,36],perform:10,pixhawkpx4:[3,36],place:49,plaform:63,plan:[36,65],platform:[3,12,13,23,36,63],platformmanag:[3,36],platformmodel:[3,36],point:68,port:18,posit:[4,10,13,68,69],preflight:35,prevent:23,provid:[1,10,18,27,45,48,60,63],queri:10,radiat:45,radiationmap:[36,37],recycl:40,region:[36,37,45],regionconfigur:[36,71],repres:[4,33,40,44,45,48,75,77,84],represent:[48,63],request:33,requir:40,resourc:[36,40,44,45,57,94],resourcemanag:[36,59],result:[1,36,37,48,75],resultavail:1,resultitem:[36,71],resulttostr:48,retriev:10,returntohom:[13,36,37],returntolaunch:4,rover:10,runplan:[36,37],scale:69,schedul:[36,37],schema:[23,27,33],seal:1,select:1,sender:33,sendersystem:23,sens:1,serial:[12,18],serialdatachannel:[3,36],serialdatachannelfactori:[3,36],serialplatform:[3,23,36],set:[13,36,71],set_mod:32,settingsact:[36,71],settingsfrag:[36,71],sever:63,shall:10,share:[4,10,13],should:[27,57],shown:48,signal:1,simpl:[33,68,69],sinc:[1,4,10,12,13,18,23,27,32,33,35,39,40,44,45,46,47,48,49,50,51,57,60,61,63,68,69],singl:48,softwar:[1,4,10,12,13,18,23,27,33,35,39,40,44,45,46,47,49,50,51,57,60,61,63,68,69],some:10,sort:10,specif:[10,27],specifi:[4,10,23,57],speed:13,speedlimit:[36,37],speedlimitconfigur:[36,71],stabilize_arm:35,stabilize_disarm:35,start:1,state:63,statu:[63,77],strategi:10,string:[10,40,47,48,61,63],structur:[40,48],submarin:10,submit:1,subset:10,subsystem:[36,94],support:[3,10,36,94],system:[1,23,33],tag:[1,12],take:[4,49],takeoff:[4,23,36,37],target:[33,36,37],targetconfigur:[36,71],targetsystem:23,task:[10,36,37,39,40],test_arm:35,test_disarm:35,thei:60,thi:[1,4,10,12,13,18,23,27,35,40,44,45,46,47,48,49,50,51,57,63],through:10,translat:[39,40],travel:13,triggerpayload:[36,37],type:[1,33,60,61,68,69],unavail:[1,63],underli:1,uniqu:[40,63],unman:10,usb:[18,36,65],usbserialport:18,use:18,used:[1,12,32,46,47,49,50,51],user:[1,36,48,94],using:45,val:[1,10,23,40,44,45,63],valu:[4,33,60,68,69],vehicl:[3,4,10,12,23,27,32,35,36,46,47,50,51,57],vehiclemod:[3,36],view:40,wai:60,want:1,wgs89:69,wgs89posit:[36,65,68],what:[60,61],when:48,which:[10,49,57],work:10,yet:10},titles:["Documentation for GCS.kt","Documentation for MainModel.kt","Documentation for ResourceModel.kt","Driver Subsystem","Documentation for driver/AerialVehicle.kt","Documentation for driver/Command.kt","Documentation for driver/Input.kt","Documentation for driver/NullCommand.kt","Documentation for driver/NullPlatform.kt","Documentation for driver/Payload.kt","Documentation for driver/Platform.kt","Documentation for driver/PlatformModel.kt","Documentation for driver/SerialPlatform.kt","Documentation for driver/Vehicle.kt","Documentation for driver/access/InputManager.kt","Documentation for driver/access/PayloadProvider.kt","Documentation for driver/access/PlatformManager.kt","Documentation for driver/channel/ChannelFactory.kt","Documentation for driver/channel/SerialDataChannel.kt","Documentation for driver/channel/SerialDataChannelFactory.kt","Documentation for driver/input/HandheldControls.kt","Documentation for driver/mavlink/MAVLinkCommand.kt","Documentation for driver/mavlink/MAVLinkPayload.kt","Documentation for driver/mavlink/MAVLinkPlatform.kt","Documentation for driver/mavlink/payload/Gripper.kt","Documentation for driver/mavlink/payload/NullPayload.kt","Documentation for driver/mavlink/platform/BasicPlatform.kt","Documentation for driver/mavlink/platform/CommonPlatform.kt","Documentation for driver/mavlink/platform/PixhawkPX4.kt","Documentation for driver/mavlink/support/LongCommand.kt","Documentation for driver/mavlink/support/MAVLinkExecution.kt","Documentation for driver/mavlink/support/MAVLinkMissionCommand.kt","Documentation for driver/mavlink/support/MessageID.kt","Documentation for driver/mavlink/support/Messages.kt","Documentation for driver/mavlink/support/NavigationFrame.kt","Documentation for driver/mavlink/support/VehicleMode.kt","Implementation","Mission Handling Subsystem","Documentation for mission/Execution.kt","Documentation for mission/Mission.kt","Documentation for mission/Need.kt","Documentation for mission/Result.kt","Documentation for mission/Scheduler.kt","Documentation for mission/access/NeedManager.kt","Documentation for mission/need/CallIn.kt","Documentation for mission/need/RadiationMap.kt","Documentation for mission/need/parameter/Altitude.kt","Documentation for mission/need/parameter/Cargo.kt","Documentation for mission/need/parameter/Parameter.kt","Documentation for mission/need/parameter/Region.kt","Documentation for mission/need/parameter/SpeedLimit.kt","Documentation for mission/need/parameter/Target.kt","Documentation for mission/need/task/LimitTravelSpeed.kt","Documentation for mission/need/task/MoveToPosition.kt","Documentation for mission/need/task/ReturnToHome.kt","Documentation for mission/need/task/RunPlan.kt","Documentation for mission/need/task/TakeOff.kt","Documentation for mission/need/task/Task.kt","Documentation for mission/need/task/TriggerPayload.kt","Resource Management Subsystem","Documentation for resource/Capability.kt","Documentation for resource/CapabilityDescriptor.kt","Documentation for resource/LocalResource.kt","Documentation for resource/Resource.kt","Documentation for resource/ResourceManager.kt","Generic Support Infrastructure","Documentation for support/concurrent/Concurrent.kt","Documentation for support/file/Plan.kt","Documentation for support/geo/GPSPosition.kt","Documentation for support/geo/WGS89Position.kt","Documentation for support/usb/DeviceHandler.kt","User Interface","Documentation for ui/MainActivity.kt","Documentation for ui/MenuFragmentID.kt","Documentation for ui/SettingsActivity.kt","Documentation for ui/mission/MissionResultsFragment.kt","Documentation for ui/mission/MissionResultsRecyclerViewAdapter.kt","Documentation for ui/mission/MissionStatusesFragment.kt","Documentation for ui/mission/MissionStatusesRecyclerViewAdapter.kt","Documentation for ui/mission/ResultItem.kt","Documentation for ui/mission/need/NeedInstructionFragment.kt","Documentation for ui/mission/need/NeedInstructionRecyclerViewAdapter.kt","Documentation for ui/mission/need/NeedItem.kt","Documentation for ui/mission/need/NeedItemFactory.kt","Documentation for ui/mission/need/NeedsFragment.kt","Documentation for ui/mission/need/NeedsRecyclerViewAdapter.kt","Documentation for ui/mission/need/parameter/ParameterItem.kt","Documentation for ui/mission/need/parameter/ParameterItemFactory.kt","Documentation for ui/mission/need/parameter/configurator/AltitudeConfigurator.kt","Documentation for ui/mission/need/parameter/configurator/CargoConfigurator.kt","Documentation for ui/mission/need/parameter/configurator/RegionConfigurator.kt","Documentation for ui/mission/need/parameter/configurator/SpeedLimitConfigurator.kt","Documentation for ui/mission/need/parameter/configurator/TargetConfigurator.kt","Documentation for ui/settings/SettingsFragment.kt","HSR HMI for Android"],titleterms:{GCS:0,access:[14,15,16,43],aerialvehicl:4,altitud:46,altitudeconfigur:88,android:94,basicplatform:26,callin:44,capabilitydescriptor:61,capabl:60,cargo:47,cargoconfigur:89,channel:[17,18,19],channelfactori:17,command:5,commonplatform:27,concurr:66,configur:[88,89,90,91,92],devicehandl:70,document:[0,1,2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,60,61,62,63,64,66,67,68,69,70,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93],driver:[3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35],execut:38,file:67,gener:65,geo:[68,69],gpsposit:68,gripper:24,handheldcontrol:20,handl:37,hmi:94,hsr:94,implement:36,infrastructur:65,input:[6,20],inputmanag:14,interfac:71,limittravelspe:52,localresourc:62,longcommand:29,mainact:72,mainmodel:1,manag:59,mavlink:[21,22,23,24,25,26,27,28,29,30,31,32,33,34,35],mavlinkcommand:21,mavlinkexecut:30,mavlinkmissioncommand:31,mavlinkpayload:22,mavlinkplatform:23,menufragmentid:73,messag:33,messageid:32,mission:[37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92],missionresultsfrag:75,missionresultsrecyclerviewadapt:76,missionstatusesfrag:77,missionstatusesrecyclerviewadapt:78,movetoposit:53,navigationfram:34,need:[40,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,80,81,82,83,84,85,86,87,88,89,90,91,92],needinstructionfrag:80,needinstructionrecyclerviewadapt:81,needitem:82,needitemfactori:83,needmanag:43,needsfrag:84,needsrecyclerviewadapt:85,nullcommand:7,nullpayload:25,nullplatform:8,paramet:[46,47,48,49,50,51,86,87,88,89,90,91,92],parameteritem:86,parameteritemfactori:87,payload:[9,24,25],payloadprovid:15,pixhawkpx4:28,plan:67,platform:[10,26,27,28],platformmanag:16,platformmodel:11,radiationmap:45,region:49,regionconfigur:90,resourc:[59,60,61,62,63,64],resourcemanag:64,resourcemodel:2,result:41,resultitem:79,returntohom:54,runplan:55,schedul:42,serialdatachannel:18,serialdatachannelfactori:19,serialplatform:12,set:93,settingsact:74,settingsfrag:93,speedlimit:50,speedlimitconfigur:91,subsystem:[3,37,59],support:[29,30,31,32,33,34,35,65,66,67,68,69,70],takeoff:56,target:51,targetconfigur:92,task:[52,53,54,55,56,57,58],triggerpayload:58,usb:70,user:71,vehicl:13,vehiclemod:35,wgs89posit:69}})