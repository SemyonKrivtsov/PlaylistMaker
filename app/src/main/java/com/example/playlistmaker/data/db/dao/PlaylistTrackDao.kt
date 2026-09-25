package com.example.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.playlistmaker.data.db.entity.PlaylistTrackCrossRef
import com.example.playlistmaker.data.db.entity.PlaylistTrackEntity

@Dao
abstract class PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef)

    @Transaction
    open suspend fun addTrackToPlaylist(track: PlaylistTrackEntity, playlistId: Long) {
        insertPlaylistTrack(track)
        insertCrossRef(PlaylistTrackCrossRef(playlistId, track.trackId, track.addedAt))
    }
}
