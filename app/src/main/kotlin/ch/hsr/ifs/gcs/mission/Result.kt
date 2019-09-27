package ch.hsr.ifs.gcs.mission

data class Result(val mission: Mission, val data: Data<*>) {

    data class Data<T>(val nativeData: T)

}
