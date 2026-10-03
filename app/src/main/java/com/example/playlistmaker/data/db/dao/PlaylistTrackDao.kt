package com.example.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.playlistmaker.data.db.entity.PlaylistTrackCrossRef
import com.example.playlistmaker.data.db.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

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

    @Query(
        """
        SELECT t.* FROM playlist_track_table t
        INNER JOIN playlist_track_cross_ref c ON t.trackId = c.trackId
        WHERE c.playlistId = :playlistId
        ORDER BY c.addedAt DESC
        """
    )
    abstract fun getPlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    abstract suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_track_cross_ref WHERE trackId = :trackId)")
    abstract suspend fun isTrackInAnyPlaylist(trackId: Long): Boolean

    @Query("DELETE FROM playlist_track_table WHERE trackId = :trackId")
    abstract suspend fun deleteTrack(trackId: Long)
}
