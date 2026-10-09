package world.respect.shared.domain.phonenumber


interface IPhoneNumber {
    val countryCode: Int
    val nationalNumber: Long
}