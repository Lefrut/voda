import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before


@OptIn(ExperimentalCoroutinesApi::class)
abstract class CoroutineTestBase() {

    protected val scheduler = TestCoroutineScheduler()
    protected val dispatcher = StandardTestDispatcher(scheduler)




    @Before
    open fun setUpBase(){
        Dispatchers.setMain(dispatcher)
    }

    @After
    open fun tearDownBase(){
        Dispatchers.resetMain()
    }
}