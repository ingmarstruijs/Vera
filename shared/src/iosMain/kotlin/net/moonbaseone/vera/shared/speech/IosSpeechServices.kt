package net.moonbaseone.vera.shared.speech

/**
 * iOS speech stubs in Shared.framework.
 *
 * Full SFSpeech / AVSpeechSynthesizer UX lives in SwiftUI (`SpeechCoordinator`)
 * until the XCFramework is linked and AVFoundation bridges are wired here.
 * These fakes keep Koin / AppointmentsViewModel constructible on iosMain.
 */
class IosSpeechToText : SpeechToText by FakeSpeechToText(available = false)

class IosTextToSpeech : TextToSpeech by FakeTextToSpeech()
