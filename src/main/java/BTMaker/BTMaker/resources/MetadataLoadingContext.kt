package BTMaker.BTMaker.resources

data class MetadataLoadingContext(
    var spritesheets: List<Resource>,
    var baseSpritesheetId: Short = 0,
    var is16Bit: Boolean = false
)