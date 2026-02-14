import { FavoriteModel, type Favorite } from '../models/favorite';
import type { FavoriteEventPayload, FavoriteEvent } from '../types';

type FavoriteRecord = Favorite & {
  eventId: string;
  createdAt?: Date | string;
};

const toFavoriteEvent = (favorite: FavoriteRecord): FavoriteEvent => {
  const createdAtValue = favorite.createdAt ?? new Date();
  const createdAt =
    createdAtValue instanceof Date
      ? createdAtValue
      : new Date(createdAtValue);

  return {
    id: favorite.eventId,
    name: favorite.name ?? '',
    date: favorite.date ?? '',
    time: favorite.time ?? '',
    venue: favorite.venue ?? '',
    genre: favorite.genre ?? '',
    image: favorite.image ?? '',
    url: favorite.url ?? '',
    createdAt: createdAt.toISOString(),
  };
};

export const getFavorites = async (): Promise<FavoriteEvent[]> => {
  const favorites = await FavoriteModel.find().sort({ createdAt: 1 }).lean().exec();
  return favorites.map((favorite) => toFavoriteEvent(favorite as FavoriteRecord));
};

export const addFavorite = async (
  payload: FavoriteEventPayload
): Promise<{ favorite: FavoriteEvent; created: boolean }> => {
  const existing = await FavoriteModel.findOne({ eventId: payload.id }).lean().exec();

  const updated = await FavoriteModel.findOneAndUpdate(
    { eventId: payload.id },
    {
      eventId: payload.id,
      name: payload.name,
      date: payload.date,
      time: payload.time,
      venue: payload.venue,
      genre: payload.genre,
      image: payload.image,
      url: payload.url,
    },
    {
      new: true,
      upsert: true,
      setDefaultsOnInsert: true,
    }
  )
    .lean()
    .exec();

  if (!updated) {
    throw new Error('Failed to upsert favorite');
  }

  return { favorite: toFavoriteEvent(updated as FavoriteRecord), created: !existing };
};

export const removeFavorite = async (eventId: string): Promise<FavoriteEvent | null> => {
  const removed = await FavoriteModel.findOneAndDelete({ eventId }).lean().exec();

  if (!removed) {
    return null;
  }

  return toFavoriteEvent(removed as FavoriteRecord);
};

// Ensure unique index on eventId to prevent duplicates (best-effort at startup)
FavoriteModel.collection.createIndex({ eventId: 1 }, { unique: true }).catch((err) => {
  console.error('Failed to create unique index on favorites.eventId:', err);
});

