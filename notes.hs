import Data.Maybe

type Image = String
type Duration = Int
type Point = (Int, Int)


data AppState = AppState { views   :: [View]
                         , curView :: View
                         } deriving (Show)

runningTimers :: AppState -> [Timer]
runningTimers (AppState vs _ ) = filter isRunning $ concatMap timers vs


data View = View { timers :: [Timer]
                 , image :: Image
                 } deriving (Show)

data Timer = Timer { total :: Duration
                   , location :: Point
                   , remaining :: Maybe Duration
                   } deriving (Show)

mkTimer :: Duration -> Point -> Timer
mkTimer t loc = Timer { total = t
                      , location = loc
                      , remaining = Nothing
                      }

startTimer :: Timer -> Timer
startTimer (Timer t loc Nothing) = Timer t loc $ Just t
startTimer t = t

stopTimer :: Timer -> Timer
stopTimer timer@(Timer _ _ Nothing) = timer
stopTimer (Timer t loc _ ) = Timer t loc Nothing

isRunning :: Timer -> Bool
isRunning (Timer _ _ (Just _ )) = True
isRunning _ = False