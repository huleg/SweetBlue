package com.idevicesinc.sweetblue;


import com.idevicesinc.sweetblue.BleManager.UhOhListener.UhOh;


final class P_Task_DiscoverServices extends PA_Task_RequiresConnection
{

	private int m_gattStatus = BleStatuses.GATT_STATUS_NOT_APPLICABLE;
	private boolean m_gattRefresh;
	private double m_curGattDelay;
	private double m_gattDelayTarget;
	private boolean m_discoverAttempted;

	
	public P_Task_DiscoverServices(BleDevice bleDevice, I_StateListener listener)
	{
		super(bleDevice, listener);
		m_gattRefresh = BleDeviceConfig.bool(getDevice().conf_device().useGattRefresh, getDevice().conf_mngr().useGattRefresh);
		m_gattDelayTarget = BleDeviceConfig.interval(getDevice().conf_device().gattRefreshDelay, getDevice().conf_mngr().gattRefreshDelay).secs();
	}

	@Override public void execute()
	{
//		if( !getDevice().getNativeGatt().getServices().isEmpty() )
		{

			if( m_gattRefresh )
			{
				getDevice().gattLayer().refreshGatt();
				return;
			}
		}
		
		if( !getDevice().gattLayer().discoverServices() )
		{
			failImmediately();
			
			getManager().uhOh(UhOh.SERVICE_DISCOVERY_IMMEDIATELY_FAILED);
		}
		m_discoverAttempted = true;
	}

	@Override protected void update(double timeStep)
	{
		if (m_gattRefresh && !m_discoverAttempted)
		{
			m_curGattDelay += timeStep;
			if (m_curGattDelay >= m_gattDelayTarget)
			{
				m_discoverAttempted = true;
				if( !getDevice().gattLayer().discoverServices() )
				{
					failImmediately();

					getManager().uhOh(UhOh.SERVICE_DISCOVERY_IMMEDIATELY_FAILED);
				}
			}
		}
	}

	@Override public PE_TaskPriority getPriority()
	{
		return PE_TaskPriority.MEDIUM;
	}
	
	public void onNativeFail(int gattStatus)
	{
		m_gattStatus = gattStatus;
		
		this.fail();
	}
	
	public int getGattStatus()
	{
		return m_gattStatus;
	}
	
	@Override protected BleTask getTaskType()
	{
		return BleTask.DISCOVER_SERVICES;
	}
}
