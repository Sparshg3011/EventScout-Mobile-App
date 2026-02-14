import { Router } from 'express';
import eventRoutes from './eventRoutes';
import favoriteRoutes from './favoriteRoutes';
import geocodingRoutes from './geocodingRoutes';

const router = Router();

router.use('/events', eventRoutes);
router.use('/favorites', favoriteRoutes);
router.use('/geo', geocodingRoutes);

export default router;

